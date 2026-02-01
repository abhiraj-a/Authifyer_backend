    package com.Auth.JWT;

    import com.Auth.Entity.JWTKey;
    import com.Auth.Repo.JWTKeyRepo;
    import com.auth0.jwt.algorithms.Algorithm;
    import jakarta.annotation.PostConstruct;
    import lombok.Data;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Component;
    import java.security.KeyFactory;
    import java.security.KeyPair;
    import java.security.KeyPairGenerator;
    import java.security.NoSuchAlgorithmException;
    import java.security.interfaces.RSAPrivateKey;
    import java.security.interfaces.RSAPublicKey;
    import java.security.spec.PKCS8EncodedKeySpec;
    import java.security.spec.X509EncodedKeySpec;
    import java.time.LocalDate;
    import java.util.Base64;
    import java.util.List;

    @Slf4j
    @Component
    @Data
    public class JWTKeyProvider {
        private Algorithm algorithm;
        private String kid;
        private  RSAPublicKey publicKey;
        private final JWTKeyRepo jwtKeyRepo;

        @PostConstruct
        public  void init(){
            List<JWTKey> keys =jwtKeyRepo.findAllByIsActiveTrue();
            if(keys.isEmpty()){
                generateKey();
                log.info("Generating new key");
            }
            else {
                loadKey(keys.getFirst());
            }
        }
        public String getKeyId() {
            return kid;
        }

        private void generateKey(){
            try {
                KeyPairGenerator keyPairGenerator =KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(4096);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();

                RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();
                RSAPrivateKey priv = (RSAPrivateKey) keyPair.getPrivate();
                String kid = "rsa-"+LocalDate.now();
                this.publicKey = pub;
                this.kid=kid;
                this.algorithm = Algorithm.RSA256(pub,priv);

                JWTKey jwtKey = JWTKey.builder()
                        .kid(kid)
                        .privateKeyPem(Base64.getEncoder().encodeToString(priv.getEncoded()))
                        .publicKeyPem(Base64.getEncoder().encodeToString(pub.getEncoded()))
                        .isActive(true)
                        .build();
                jwtKeyRepo.save(jwtKey);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }


        private void loadKey(JWTKey key){

            KeyFactory kf = null;
            try {
                kf = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec pubkeySpec  = new X509EncodedKeySpec(Base64.getDecoder().decode(key.getPublicKeyPem()));
                RSAPublicKey pub = (RSAPublicKey) kf.generatePublic(pubkeySpec);
                PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key.getPrivateKeyPem()));
                RSAPrivateKey priv = (RSAPrivateKey) kf.generatePrivate(privSpec);
                this.kid = key.getKid();
                this.algorithm = Algorithm.RSA256(pub,priv);
                this.publicKey = pub;

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
