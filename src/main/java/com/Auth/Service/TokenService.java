package com.Auth.Service;
import com.Auth.Entity.GlobalUser;
import com.Auth.Entity.Session;
import com.Auth.JWT.AccessTokenClaims;
import com.Auth.JWT.JWTKeyProvider;
import com.Auth.Repo.GlobalUserRepo;
import com.Auth.Repo.SessionRepo;
import com.Auth.Util.TokenHash;
import com.auth0.jwt.JWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTKeyProvider jwtKeyProvider;

    private final SessionRepo sessionRepo;

    private final TokenHash TokenHash;
    private final GlobalUserRepo globalUserRepo;

    public String generateRefreshToken() {
        SecureRandom secureRandom =new SecureRandom();
        byte[] bytes =new byte[128];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public AccessTokenClaims issueGlobalAccessToken(String refreshToken){

        Session session = sessionRepo.findByTokenHash(TokenHash.hash(refreshToken)).orElseThrow(RuntimeException::new);

        if (session.getRevokedAt() != null) {
            throw new RuntimeException("Session revoked");
        }
        Instant now = Instant.now();

        GlobalUser user = globalUserRepo.findBySubjectId(session.getSubjectId()).orElseThrow(RuntimeException::new);
       String jwt=  JWT.create()
                .withSubject(session.getSubjectId())
                .withKeyId(jwtKeyProvider.getKeyId())
                .withClaim("sid",session.getPublicId())
                .withClaim("scope" , "global")
                .withClaim("email",user.getEmail())
                .withClaim("name",user.getName())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(60)))
                .withIssuer("http://localhost:8080")
                .sign(jwtKeyProvider.getAlgorithm());
         return AccessTokenClaims.builder()
                 .accessToken(jwt)
                 .issued_at(Instant.now())
                 .expires_at(Instant.now().plusSeconds(60))
                 .build();

    }

    public AccessTokenClaims issueAccessToken(String refreshToken) {
        Session session = sessionRepo.findByTokenHash(TokenHash.hash(refreshToken)).orElseThrow(RuntimeException::new);


        if (session.getRevokedAt() != null) {
            throw new RuntimeException("Session revoked");
        }
        if(session.getPublicProjectId()==null){
            return issueGlobalAccessToken(refreshToken);
        }

        Instant now = Instant.now();

       String accessToken = JWT.create()
               .withKeyId(jwtKeyProvider.getKeyId())
                .withSubject(session.getSubjectId())
                .withClaim("sid",session.getPublicId())
                .withClaim("pid" , session.getPublicProjectId())
                .withClaim("scope" , "project")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(60)))
                .withIssuer("http://localhost:8080")
                .sign(jwtKeyProvider.getAlgorithm());
       return AccessTokenClaims.builder()
               .accessToken(accessToken)
               .issued_at(Instant.now())
               .expires_at(Instant.now().plusSeconds(60))
               .build();
    }


}
