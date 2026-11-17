
package pe.edu.upeu.PPPBACKEND.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.upeu.PPPBACKEND.repository.UsuarioRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GoogleDriveOAuthService {

    private static final String GOOGLE_AUTH_URL =
            "https://accounts.google.com/o/oauth2/v2/auth";

    private static final String GOOGLE_TOKEN_URL =
            "https://oauth2.googleapis.com/token";

    // ==========================================
    // CONFIGURACION DE GOOGLE OAUTH
    // ==========================================

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String scope;

    private final UsuarioRepository usuarioRepository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    private final RestClient restClient =
            RestClient.create();

    // ==========================================
    // SOLICITUDES OAUTH TEMPORALES
    // ==========================================

    private final Map<String, SolicitudOAuth> solicitudes =
            new ConcurrentHashMap<>();

    // Conexion temporal para el prototipo local.
    // Los tokens permanecen en memoria.
    // No se guardan en Oracle ni se exponen a Angular.
    private volatile TokenGoogle tokenActual;

    private record SolicitudOAuth(
            Long idCoordinador,
            String codeVerifier,
            Instant expiracion
    ) {}

    private record TokenGoogle(
            String accessToken,
            String refreshToken,
            Instant expiracion,
            Long idCoordinador
    ) {}

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public GoogleDriveOAuthService(
            @Value("${google.drive.client-id}") String clientId,
            @Value("${google.drive.client-secret}") String clientSecret,
            @Value("${google.drive.redirect-uri}") String redirectUri,
            @Value("${google.drive.scope}") String scope,
            UsuarioRepository usuarioRepository) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.scope = scope;
        this.usuarioRepository = usuarioRepository;
    }

    // ==========================================
    // GENERAR VALORES ALEATORIOS SEGUROS
    // ==========================================

    private String generarAleatorio() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // ==========================================
    // CREAR DESAFIO PKCE
    // ==========================================

    private String generarCodeChallenge(String verifier) {

        try {

            byte[] hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(
                            verifier.getBytes(
                                    StandardCharsets.US_ASCII
                            )
                    );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "No se pudo generar el desafio PKCE",
                    ex
            );
        }
    }

    // ==========================================
    // CODIFICAR PARAMETROS DE URL
    // ==========================================

    private String codificar(String valor) {

        return URLEncoder.encode(
                valor,
                StandardCharsets.UTF_8
        );
    }

    // ==========================================
    // INICIAR AUTORIZACION DE GOOGLE DRIVE
    // ==========================================

    public String crearUrlAutorizacion(Long idCoordinador) {

        validarCoordinador(idCoordinador);

        // Eliminar solicitudes OAuth caducadas.
        solicitudes.entrySet().removeIf(
                entrada -> Instant.now().isAfter(
                        entrada.getValue().expiracion()
                )
        );

        if (solicitudes.size() >= 100) {

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiadas autorizaciones pendientes"
            );
        }

        // Valor aleatorio para proteger la solicitud.
        String state = generarAleatorio();

        // Generar PKCE.
        String codeVerifier = generarAleatorio();

        String codeChallenge =
                generarCodeChallenge(codeVerifier);

        // Almacenar temporalmente la solicitud.
        solicitudes.put(
                state,
                new SolicitudOAuth(
                        idCoordinador,
                        codeVerifier,
                        Instant.now().plusSeconds(600)
                )
        );

        // Construir URL de autorizacion de Google.
        return GOOGLE_AUTH_URL
                + "?client_id=" + codificar(clientId)
                + "&redirect_uri=" + codificar(redirectUri)
                + "&response_type=code"
                + "&scope=" + codificar(scope)
                + "&access_type=offline"
                + "&prompt=consent"
                + "&code_challenge=" + codificar(codeChallenge)
                + "&code_challenge_method=S256"
                + "&state=" + codificar(state);
    }

    // ==========================================
    // RECIBIR AUTORIZACION DE GOOGLE
    // ==========================================

    public synchronized void completarAutorizacion(
            String state,
            String code) {

        if (
                state == null || state.isBlank() ||
                        code == null || code.isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Autorizacion OAuth incompleta"
            );
        }

        // El state solo puede utilizarse una vez.
        SolicitudOAuth solicitud = solicitudes.remove(state);

        if (
                solicitud == null ||
                        Instant.now().isAfter(solicitud.expiracion())
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Autorizacion OAuth invalida o caducada"
            );
        }

        // Verificar el estado actual del coordinador.
        validarCoordinador(solicitud.idCoordinador());

        // ======================================
        // INTERCAMBIAR CODIGO POR TOKENS
        // ======================================

        MultiValueMap<String, String> formulario =
                new LinkedMultiValueMap<>();

        formulario.add("client_id", clientId);
        formulario.add("client_secret", clientSecret);
        formulario.add("code", code);
        formulario.add("redirect_uri", redirectUri);
        formulario.add("grant_type", "authorization_code");
        formulario.add(
                "code_verifier",
                solicitud.codeVerifier()
        );

        Map<?, ?> respuesta;

        try {

            respuesta = restClient.post()
                    .uri(GOOGLE_TOKEN_URL)
                    .contentType(
                            MediaType.APPLICATION_FORM_URLENCODED
                    )
                    .body(formulario)
                    .retrieve()
                    .body(Map.class);

        } catch (Exception ex) {

            // No mostrar codigos ni credenciales.
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google no pudo completar la autorizacion"
            );
        }

        if (respuesta == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google no devolvio una respuesta valida"
            );
        }

        // ======================================
        // EXTRAER TOKENS
        // ======================================

        String accessToken =
                obtenerTexto(respuesta.get("access_token"));

        String refreshToken =
                obtenerTexto(respuesta.get("refresh_token"));

        // Si Google no devuelve otro refresh token,
        // reutilizar el anterior del mismo coordinador.
        if (
                refreshToken.isBlank() &&
                        tokenActual != null &&
                        tokenActual.idCoordinador()
                                .equals(solicitud.idCoordinador())
        ) {

            refreshToken = tokenActual.refreshToken();
        }

        if (
                accessToken.isBlank() ||
                        refreshToken.isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google no entrego los tokens necesarios"
            );
        }

        long segundos = obtenerDuracionToken(respuesta);

        // ======================================
        // GUARDAR CONEXION EN MEMORIA
        // ======================================

        tokenActual = new TokenGoogle(
                accessToken,
                refreshToken,
                Instant.now().plusSeconds(segundos),
                solicitud.idCoordinador()
        );
    }

    // ==========================================
    // COMPROBAR SI EXISTE UNA CONEXION
    // ==========================================

    public boolean estaConectado() {

        TokenGoogle actual = tokenActual;

        return actual != null &&
                !actual.refreshToken().isBlank();
    }

    // ==========================================
    // NUEVO METODO:
    // OBTENER TOKEN VALIDO PARA SUBIR A DRIVE
    // ==========================================

    public synchronized String obtenerTokenAccesoParaSubida() {

        TokenGoogle actual = tokenActual;

        // ======================================
        // 1. COMPROBAR CONEXION
        // ======================================

        if (
                actual == null ||
                        actual.refreshToken().isBlank()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Google Drive no esta conectado"
            );
        }

        // ======================================
        // 2. VALIDAR AL COORDINADOR
        // ======================================

        // La cuenta que autorizo Google Drive
        // debe continuar activa en Oracle.
        validarCoordinador(actual.idCoordinador());

        // ======================================
        // 3. COMPROBAR EXPIRACION DEL TOKEN
        // ======================================

        // Si quedan mas de 60 segundos de validez,
        // reutilizar el access token existente.
        if (
                Instant.now().isBefore(
                        actual.expiracion().minusSeconds(60)
                )
        ) {

            return actual.accessToken();
        }

        // ======================================
        // 4. PREPARAR RENOVACION
        // ======================================

        MultiValueMap<String, String> formulario =
                new LinkedMultiValueMap<>();

        formulario.add("client_id", clientId);
        formulario.add("client_secret", clientSecret);
        formulario.add("refresh_token", actual.refreshToken());
        formulario.add("grant_type", "refresh_token");

        Map<?, ?> respuesta;

        // ======================================
        // 5. SOLICITAR NUEVO ACCESS TOKEN
        // ======================================

        try {

            respuesta = restClient.post()
                    .uri(GOOGLE_TOKEN_URL)
                    .contentType(
                            MediaType.APPLICATION_FORM_URLENCODED
                    )
                    .body(formulario)
                    .retrieve()
                    .body(Map.class);

        } catch (Exception ex) {

            // No exponer el refresh token ni secretos.
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudo renovar la autorizacion de Google Drive"
            );
        }

        if (respuesta == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google no devolvio un token valido"
            );
        }

        // ======================================
        // 6. OBTENER NUEVO ACCESS TOKEN
        // ======================================

        String nuevoAccessToken =
                obtenerTexto(respuesta.get("access_token"));

        if (nuevoAccessToken.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google no entrego un nuevo token de acceso"
            );
        }

        long segundos = obtenerDuracionToken(respuesta);

        // ======================================
        // 7. CONSERVAR O RENOVAR REFRESH TOKEN
        // ======================================

        String nuevoRefreshToken =
                obtenerTexto(respuesta.get("refresh_token"));

        if (nuevoRefreshToken.isBlank()) {

            nuevoRefreshToken = actual.refreshToken();
        }

        // ======================================
        // 8. ACTUALIZAR TOKENS EN MEMORIA
        // ======================================

        tokenActual = new TokenGoogle(
                nuevoAccessToken,
                nuevoRefreshToken,
                Instant.now().plusSeconds(segundos),
                actual.idCoordinador()
        );

        // ======================================
        // 9. DEVOLVER ACCESS TOKEN VALIDO
        // ======================================

        return nuevoAccessToken;
    }

    // ==========================================
    // VALIDAR EL COORDINADOR AUTORIZADO
    // ==========================================

    private void validarCoordinador(Long idUsuario) {

        boolean permitido = usuarioRepository
                .findById(idUsuario)
                .map(usuario ->
                        "COORDINADOR_PPP".equals(usuario.getRol()) &&
                                "ACTIVO".equals(usuario.getEstado())
                )
                .orElse(false);

        if (!permitido) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo un coordinador activo puede conectar Google Drive"
            );
        }
    }

    // ==========================================
    // OBTENER DURACION DE UN TOKEN
    // ==========================================

    private long obtenerDuracionToken(Map<?, ?> respuesta) {

        long segundos = 3600;

        Object expiresIn = respuesta.get("expires_in");

        if (expiresIn instanceof Number numero) {
            segundos = numero.longValue();
        }

        if (segundos <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google devolvio un tiempo de validez invalido"
            );
        }

        return segundos;
    }

    // ==========================================
    // CONVERTIR RESPUESTAS DE GOOGLE
    // ==========================================

    private String obtenerTexto(Object valor) {

        return valor instanceof String texto
                ? texto
                : "";
    }
}
