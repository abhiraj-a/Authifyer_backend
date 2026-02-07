package com.Auth.Service;
import com.Auth.Entity.*;
import com.Auth.OAuth2.OAuthProfile;
import com.Auth.Repo.*;
import com.Auth.Util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepo sessionRepo;

    private final TokenService tokenService;

    private final GlobalUserRepo globalUserRepo;

    private final ProjectUserRepo projectUserRepo;

    private final ProjectRepo projectRepo;

    private final OAuthStorageRepo oAuthStorageRepo;

    private final TokenHash TokenHash;
    public RefreshResult createSession(String authifyerId, String projectId,  HttpServletRequest servletRequest, HttpServletResponse response) {

        String refreshToken = tokenService.generateRefreshToken();
        Session session = Session.builder()
                .userAgent(servletRequest.getHeader("User-Agent"))
                .deviceName(Extractor.parseDeviceName(servletRequest))
                .IpAddress(Extractor.getClientIP(servletRequest))
                .publicProjectId(projectId)
                .subjectId(authifyerId)
                .createdAt(Instant.now())
                .createdAt(Instant.now())
                .tokenHash(TokenHash.hash(refreshToken))
                .build();

        sessionRepo.save(session);
        RefreshCookie.set(response,refreshToken);

        return RefreshResult.builder()
                .rawRefreshToken(refreshToken)
                .session(session)
                .build();
    }

    public RefreshResult createGlobalSession(String subjectId, HttpServletRequest servletRequest, HttpServletResponse response){
        String rawRefreshToken = tokenService.generateRefreshToken();

        Session session = Session.builder()
                .publicId(IdGenerator.generatePublicSessionId())
                .sessionScope("global")
                .createdAt(Instant.now())
                .revokedAt(null)
                .IpAddress(Extractor.getClientIP(servletRequest))
                .deviceName(Extractor.parseDeviceName(servletRequest))
                .lastAccessedAt(Instant.now())
                .tokenHash(TokenHash.hash(rawRefreshToken))
                .userAgent(servletRequest.getHeader("User-Agent"))
                .subjectId(subjectId)
                .build();
        RefreshCookie.set(response,rawRefreshToken);
        sessionRepo.save(session);
        return RefreshResult.builder()
                .session(session)
                .rawRefreshToken(rawRefreshToken)
                .build();
    }

    public void revokeSession(String token) {
        Session se=sessionRepo.findByTokenHash(TokenHash.hash(token)).orElseThrow(RuntimeException::new);
        if(se.getRevokedAt().isBefore(Instant.now())) {
            throw new RuntimeException();
        }
        se.setRevokedAt(Instant.now());
    }

    public void touch(String publicSessionId) {
        Session s =sessionRepo.findByPublicId(publicSessionId).orElseThrow(RuntimeException::new);
        s.setLastAccessedAt(Instant.now());
    }

    public RefreshResult rotateSession(String token, String ip, String device, String user) {
        String tokenHash = TokenHash.hash(token);
        Session oldSession = sessionRepo.findByTokenHash(tokenHash).orElseThrow(RuntimeException::new);
        String newRefreshToken = tokenService.generateRefreshToken();
        Session newSession = Session.builder()
                .publicProjectId(oldSession.getPublicProjectId())
                .IpAddress(ip)
                .userAgent(user)
                .publicId(IdGenerator.generatePublicSessionId())
                .revokedAt(null)
                .createdAt(Instant.now())
                .subjectId(oldSession.getSubjectId())
                .tokenHash(TokenHash.hash(newRefreshToken))
                .build();
        oldSession.setRevokedAt(Instant.now());
        sessionRepo.save(oldSession);

        return RefreshResult.builder()
                .rawRefreshToken(newRefreshToken)
                .session(sessionRepo.save(newSession))
                .build();
    }

    @Cacheable(value = "sessions",key = "#publicSessionId")
    public Session getPublicIdCache(String publicId){
        return sessionRepo.findByPublicId(publicId).orElseThrow(RuntimeException::new);
    }

    @Transactional
    public RefreshResult createOAuthSession(HttpServletRequest request, OAuthProfile oAuthProfile,String publicProjectId) {
        boolean isGlobal = publicProjectId==null||publicProjectId.isBlank();
        String scope = isGlobal?"global":"project";

        String subject = null;

        if (!isGlobal) {
            projectRepo.findByPublicProjectId(publicProjectId)
                    .orElseThrow(() -> new RuntimeException("Invalid Project ID: " + publicProjectId));
        }
        if(scope.equals("global")){
            OAuthStorage oAuthStorage = oAuthStorageRepo.findByProviderAndProviderId(oAuthProfile.getProvider(), oAuthProfile.getProviderUserId()).orElse(null);
            if(oAuthStorage!=null){
                GlobalUser user = globalUserRepo.findBySubjectId(oAuthStorage.getSubjectId()).orElseThrow(RuntimeException::new);
                if (!user.isActive()) {
                    throw new RuntimeException("Account Disabled. ");
                }
                subject = oAuthStorage.getSubjectId();
            }
            else {
                GlobalUser user=null;
                if(oAuthProfile.getEmail()!=null){
                    user=globalUserRepo.findByEmail(oAuthProfile.getEmail()).orElse(null);
                }
                if(user!=null){
                    if (!user.isActive()) {
                        throw new RuntimeException("Account Disabled.");
                    }
                    OAuthStorage newLink = OAuthStorage.builder()
                            .createdAt(Instant.now())
                            .email(oAuthProfile.getEmail())
                            .provider(oAuthProfile.getProvider())
                            .providerId(oAuthProfile.getProviderUserId())
                            .subjectId(user.getSubjectId())
                            .build();

                    oAuthStorageRepo.save(newLink);
                    subject = user.getSubjectId();
                }
                else {
                    user= GlobalUser.builder()
                            .subjectId(IdGenerator.generateGlobalUserSubjectId())
                            .name(oAuthProfile.getName())
                            .email(oAuthProfile.getEmail())
                            .createdAt(Instant.now())
                            .providerUserId(oAuthProfile.getProviderUserId())
                            .provider(oAuthProfile.getProvider())
                            .build();
                    globalUserRepo.save(user);
                    OAuthStorage oAuthStorage1 = OAuthStorage.builder()
                            .providerId(user.getProviderUserId())
                            .provider(oAuthProfile.getProvider())
                            .subjectId(user.getSubjectId())
                            .email(user.getEmail())
                            .createdAt(Instant.now())
                            .build();

                    oAuthStorageRepo.save(oAuthStorage1);
                    subject= user.getSubjectId();
                }
            }
        }
        else {

            Project project = projectRepo.findByPublicProjectId(publicProjectId).orElseThrow(RuntimeException::new);
            OAuthStorage oAuthStorage = oAuthStorageRepo.findByProviderAndProviderIdAndPublicId(oAuthProfile.getProvider(), oAuthProfile.getProviderUserId(),project.getPublicProjectId()).orElse(null);
            if(oAuthStorage!=null){
                subject = oAuthStorage.getSubjectId();
            }
            else {
                ProjectUser user=null;
                if(oAuthProfile.getEmail()!=null){
                    user=projectUserRepo.findByEmail(oAuthProfile.getEmail()).orElse(null);
                }
                if(user!=null){
                    OAuthStorage newlink = OAuthStorage.builder()
                            .createdAt(Instant.now())
                            .email(oAuthProfile.getEmail())
                            .provider(oAuthProfile.getProvider())
                            .providerId(oAuthProfile.getProviderUserId())
                            .subjectId(user.getAuthifyerId())
                            .publicId(project.getPublicProjectId())
                            .build();
                    oAuthStorageRepo.save(newlink);
                    subject=user.getAuthifyerId();
                }else {
                    user = ProjectUser.builder()
                            .authifyerId(IdGenerator.generateAuthifyerId())
                            .createdAt(Instant.now())
                            .email(oAuthProfile.getEmail())
                            .provider(oAuthProfile.getProvider())
                            .providerId(oAuthProfile.getProviderUserId())
                            .project(project)
                            .build();

                    OAuthStorage oAuthStorage1 = OAuthStorage.builder()
                            .providerId(user.getProviderId())
                            .provider(oAuthProfile.getProvider())
                            .subjectId(user.getAuthifyerId())
                            .email(user.getEmail())
                            .createdAt(Instant.now())
                            .build();
                    projectUserRepo.save(user);
                    oAuthStorageRepo.save(oAuthStorage1);
                    project.getProjectUsers().add(user);
                    projectRepo.save(project);
                    subject = user.getAuthifyerId();
                }
            }

//            Project project = projectRepo.findByPublicId(publicProjectId)
//                    .orElseThrow(() -> new RuntimeException("Project not found"));
//
//            // 1. Try to find an existing user IN THIS PROJECT by Provider (Google/GitHub)
//            ProjectUser user = projectUserRepo.findByProjectAndProviderAndProviderId(
//                    project,
//                    oAuthProfile.getProvider(),
//                    oAuthProfile.getProviderUserId()
//            ).orElse(null);
//
//            // 2. If not found by Provider, try by Email (Account Linking)
//            if (user == null && oAuthProfile.getEmail() != null) {
//                user = projectUserRepo.findByProjectAndEmail(
//                        project,
//                        oAuthProfile.getEmail()
//                ).orElse(null);
//
//                // If found by email but no provider link, you might want to update the user record here
//                if (user != null) {
//                    // Optional: Update user to link this provider for future logins
//                    user.setProvider(oAuthProfile.getProvider());
//                    user.setProviderId(oAuthProfile.getProviderUserId());
//                    projectUserRepo.save(user);
//                }
//            }
//
//            // 3. If still null, CREATE a new user specific to THIS Project
//            if (user == null) {
//                user = ProjectUser.builder()
//                        .authifyerId(IdGenerator.generateAuthifyerId()) // New Unique ID
//                        .project(project) // Link to Project
//                        .createdAt(Instant.now())
//                        .email(oAuthProfile.getEmail())
//                        .name(oAuthProfile.getName())
//                        .provider(oAuthProfile.getProvider())
//                        .providerId(oAuthProfile.getProviderUserId())
//                        .build();
//
//                projectUserRepo.save(user);
//                // We don't necessarily need OAuthStorage for project users if we query ProjectUser directly
//            }
//
//            subject = user.getAuthifyerId();

        }

        String rawRefreshToken = tokenService.generateRefreshToken();


        Session session=Session.builder()
                .sessionScope(scope)
                .createdAt(Instant.now())
                .revokedAt(null)
                .IpAddress(Extractor.getClientIP(request))
                .deviceName(Extractor.parseDeviceName(request))
                .publicProjectId(isGlobal ? null : publicProjectId)
                .lastAccessedAt(Instant.now())
                .tokenHash(TokenHash.hash(rawRefreshToken))
                .userAgent(request.getHeader("User-Agent"))
                .subjectId(subject)
                .build();

        sessionRepo.save(session);
        return RefreshResult.builder()
                .session(session)
                .rawRefreshToken(rawRefreshToken)
                .build();
    }


}
