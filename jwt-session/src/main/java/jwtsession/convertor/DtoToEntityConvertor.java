package jwtsession.convertor;

import ch.qos.logback.core.util.DatePatternToRegexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jwtsession.controller.CreateTokenRequest;
import jwtsession.dao.JwtSessionDao;
import jwtsession.dao.entity.JwtSessionEntity;
import jwtsession.dateutil.DateUtil;
import jwtsession.jwtutil.JwtAccessTokenUtil;
import jwtsession.jwtutil.JwtRefreshTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.persistence.Column;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Map;

@Component("dtoToEntityConvertor")
public class DtoToEntityConvertor {
    @Autowired
    private JwtAccessTokenUtil jwtAccessTokenUtil;
    @Autowired
    private JwtRefreshTokenUtil jwtRefreshTokenUtil;

    @Autowired
    private JwtSessionDao jwtSessionDao;

    public JwtSessionEntity createTokenRequestDtoToJwtSessionEntityConversion(CreateTokenRequest request,HttpServletRequest httpServletRequest){

        String accessToken = jwtAccessTokenUtil.generateAccessToken(request.getUserId());
        String refreshToken = jwtRefreshTokenUtil.generateRefreshToken(request.getUserId());
        String tokenIdentityNumber = jwtAccessTokenUtil.getTokenIdentityNumber(accessToken);
        JwtSessionEntity entity = new JwtSessionEntity();

        if(request.getToken()!=null){
            entity.setIsActive(Boolean.FALSE);
            entity.setIsLogined(Boolean.FALSE);
            String identityNumber = getTokenIdentity(request.getToken());
            jwtSessionDao.invalidateToken(tokenIdentityNumber);
        }else{
            entity.setIsActive(Boolean.TRUE);
            entity.setIsLogined(Boolean.TRUE);
        }
        entity.setFirstName(request.getFirstName());
        entity.setUserId(request.getUserId());
        entity.setAccessTokenExpireAt(DateUtil.todayDate());
        String browserDetail = httpServletRequest.getHeader("User-Agent");
        entity.setDeviceName(browserDetail);
        entity.setClientIp(httpServletRequest.getRemoteAddr());
        entity.setHostServer(httpServletRequest.getRemoteHost());
        entity.setAccessToken(accessToken);
        entity.setTokenIdentity(tokenIdentityNumber);
        entity.setRefreshToken(refreshToken);
        return entity;
    }

    public String getTokenIdentity(String accessToken){
        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String[] parts = accessToken.split("\\."); // Splitting header, payload and signature
            String payload = new String(decoder.decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(payload, Map.class);
            return String.valueOf(map.get("identity"));
        }catch (JsonProcessingException exception){
            exception.printStackTrace();
        }
        return null;
    }
}
