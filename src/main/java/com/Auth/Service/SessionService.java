package com.Auth.Service;
import com.Auth.Entity.*;
import com.Auth.Exception.AccountSuspendedEXception;
import com.Auth.Exception.ProjectNotFoundException;
import com.Auth.Exception.SessionNotFoundException;
import com.Auth.Exception.UserNotFoundException;
import com.Auth.OAuth2.OAuthProfile;
import com.Auth.Repo.*;
import com.Auth.Util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
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

    @Transactional
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
                .publicId(IdGenerator.generatePublicSessionId())
                .build();

        sessionRepo.save(session);
        RefreshCookie.set(response,refreshToken);

        return RefreshResult.builder()
                .rawRefreshToken(refreshToken)
                .session(session)
                .build();
    }

    @Transactional
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
        Session se=sessionRepo.findByTokenHash(TokenHash.hash(token)).orElseThrow(SessionNotFoundException::new);
        if(se.getRevokedAt()!=null) {
            return;
        }
        se.setRevokedAt(Instant.now());
        sessionRepo.save(se);
    }


    @Transactional
    public RefreshResult rotateSession(String token, String ip, String device, String user) {
        String tokenHash = TokenHash.hash(token);
        Session oldSession = sessionRepo.findByTokenHash(tokenHash).orElseThrow(SessionNotFoundException::new);
        String newRefreshToken = tokenService.generateRefreshToken();
        Session newSession = Session.builder()
                .publicProjectId(oldSession.getPublicProjectId())
                .IpAddress(ip)
                .userAgent(user)
                .publicId(IdGenerator.generatePublicSessionId())
                .revokedAt(null)
                .createdAt(Instant.now())
                .deviceName(device)
                .subjectId(oldSession.getSubjectId())
                .tokenHash(TokenHash.hash(newRefreshToken))
                .publicId(IdGenerator.generatePublicSessionId())
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
        return sessionRepo.findByPublicId(publicId).orElseThrow(SessionNotFoundException::new);
    }

    @Transactional
    public RefreshResult createOAuthSession(HttpServletRequest request, OAuthProfile oAuthProfile,String publishableKey) {
        boolean isGlobal = publishableKey==null||publishableKey.isBlank();
        String scope = isGlobal?"global":"project";

        log.info("created oauth session  scope : " + scope );
        String subject = null;
        String publicProjectId=null;
//        Project project;
//        if (!isGlobal) {
//          project=  projectRepo.findByPublishableKey(publishableKey)
//                    .orElseThrow(ProjectNotFoundException::new);
//        }
        if(scope.equals("global")){
//            OAuthStorage oAuthStorage = oAuthStorageRepo.findByProviderAndProviderId(oAuthProfile.getProvider(), oAuthProfile.getProviderUserId()).orElse(null);
            OAuthStorage oAuthStorage  ;
            if(oAuthProfile.getEmail()!=null&&!oAuthProfile.getEmail().isBlank()){
                oAuthStorage =oAuthStorageRepo.findByProviderAndProviderIdAndEmailAndPublishableKeyIsNull(oAuthProfile.getProvider(),oAuthProfile.getProviderUserId() , oAuthProfile.getEmail()).orElse(null);
            }
            else {
                oAuthStorage = oAuthStorageRepo.findByProviderAndProviderIdAndPublishableKeyIsNull(oAuthProfile.getProvider(), oAuthProfile.getProviderUserId()).orElse(null);
            }

            if(oAuthStorage!=null){      //user already present
                GlobalUser user = globalUserRepo.findBySubjectId(oAuthStorage.getSubjectId()).orElseThrow(UserNotFoundException::new);
                if (!user.isActive()) {
                    throw new AccountSuspendedEXception();
                }
                subject = oAuthStorage.getSubjectId();
            }
            else {
                GlobalUser user=null;
                if(oAuthProfile.getEmail()!=null){
                    user=globalUserRepo.findByEmail(oAuthProfile.getEmail()).orElse(null);
                }
                if(user!=null){    // User has present with different oauth
                    if (!user.isActive()) {
                        throw new AccountSuspendedEXception();
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
                else {   // New User
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

            Project project = projectRepo.findByPublishableKey(publishableKey).orElseThrow(RuntimeException::new);
            publicProjectId=project.getPublicProjectId();

            OAuthStorage oAuthStorage ;
            if(oAuthProfile.getEmail()!=null&&!oAuthProfile.getEmail().isBlank()){
                oAuthStorage =oAuthStorageRepo.findByProviderAndProviderIdAndEmailAndPublishableKey(oAuthProfile.getProvider(),oAuthProfile.getProviderUserId() , oAuthProfile.getEmail(),publishableKey).orElse(null);
            }
            else {
                oAuthStorage = oAuthStorageRepo.findByProviderAndProviderIdAndPublishableKey(oAuthProfile.getProvider(), oAuthProfile.getProviderUserId(),publishableKey).orElse(null);
            }
            if(oAuthStorage!=null){
                subject = oAuthStorage.getSubjectId();
            }
            else {
                ProjectUser user=null;
                if(oAuthProfile.getEmail()!=null){
                    user=projectUserRepo.findByEmailAndProject(oAuthProfile.getEmail() ,project).orElse(null);
                }
                if(user!=null){
                    OAuthStorage newlink = OAuthStorage.builder()
                            .createdAt(Instant.now())
                            .email(oAuthProfile.getEmail())
                            .provider(oAuthProfile.getProvider())
                            .providerId(oAuthProfile.getProviderUserId())
                            .subjectId(user.getAuthifyerId())
                            .publicId(project.getPublicProjectId())
                            .publishableKey(project.getPublishableKey())
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
                            .publishableKey(project.getPublishableKey())
                            .publicId(project.getPublicProjectId())
                            .build();
                    projectUserRepo.save(user);
                    oAuthStorageRepo.save(oAuthStorage1);
                    project.getProjectUsers().add(user);
                    projectRepo.save(project);
                    subject = user.getAuthifyerId();

                }
            }

        }

        String rawRefreshToken = tokenService.generateRefreshToken();


        Session session=Session.builder()
                .sessionScope(scope)
                .createdAt(Instant.now())
                .revokedAt(null)
                .IpAddress(Extractor.getClientIP(request))
                .deviceName(Extractor.parseDeviceName(request))
                .publicProjectId(!isGlobal ? publicProjectId : null)
                .lastAccessedAt(Instant.now())
                .tokenHash(TokenHash.hash(rawRefreshToken))
                .userAgent(request.getHeader("User-Agent"))
                .subjectId(subject)
                .publicId(IdGenerator.generatePublicSessionId())
                .publishableKey(publishableKey)
                .build();

        sessionRepo.save(session);
        return RefreshResult.builder()
                .session(session)
                .rawRefreshToken(rawRefreshToken)
                .build();
    }


}
