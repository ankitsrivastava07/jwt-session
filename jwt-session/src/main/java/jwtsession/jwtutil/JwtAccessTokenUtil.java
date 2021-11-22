package jwtsession.jwtutil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import jwtsession.dao.RandomString;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtAccessTokenUtil {

	public static final long JWT_TOKEN_VALIDITY = 30 * 60 * 1;

	private String secret = "CHowlongistheencryptiondecryptionkeyforanassymetricalgorithmsuchasAESIfIuseAESbithowmanycharactersshouldItypeinformykeyWhataboutbitHoHowlongistheencryptiondecrHowlongistheencryptiondecryptionkeyforanassymetricalgorithmsuchasAESIfIuseAESbithowmanycharactersshouldItypeinformykeyWhataboutbityptionkeyforanassymetricalgorithmsuchasAESIfIuseAESbithowmanycharactersshouldItypeinformykeyWhataboutbitwlongistheencryptiondecryptionkeyforanassymetricalgorithmsuchasAESIfIuseAESbithowmanycharactersshouldItypeinformykeyWhataboutbitlosedThisquesClosedyearsagotionisofftopicItisnotcurrentlyacceptinganswersWanttoimprovethisquestionUpdatethequestionsoitsontopicforInformationSecurityStackExchangen";

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public Long getUserId(String token) {
		return Long.valueOf(getClaimFromToken(token, Claims::getSubject));
	}

	//retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	public String createAccessToken(Long userId) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, userId);
	}

	public String getTokenIdentityNumber(String accessToken) {
		Claims claims = getAllClaimsFromToken(accessToken);
		String identity = claims.get("identity",String.class);
		return identity;
	}

	private String createToken(Map<String, Object> claims, Long userId) {
		return Jwts.builder().setSubject(String.valueOf(userId))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.HS512, secret)
				.claim("identity", RandomString.getAlphaNumericString(20))
				.compact();
	}

	public void validateToken(String token) {
		isTokenExpired(token);
	}
}
