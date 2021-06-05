package jwtsession.jwtutil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtRefreshTokenUtil {

	public static final long JWT_TOKEN_VALIDITY = 60 * 30 * 1;

	private String secret = "CHowlongistheencryptiondecryptionkeyforanassymetricalgorithmsuchasAESIfIuseAESbithowmanycharactersshouldItypeinformykeyWhataboutbitHoHowlongistheencryptiondecrHowlongistheencryptiondecryptionkeyforanassymetricalgorithmsuchasAESIfIuseAESbithowmanycharactersshouldItypeinformykeyWhataboutbityptionkeyforanassymetricalgorithmsuchasAESIfIuseAESbithowmanycharactersshouldItypeinformykeyWhataboutbitwlongistheencryptiondecryptionkeyforanassymetricalgorithmsuchasAESIfIuseAESbithowmanycharactersshouldItypeinformykeyWhataboutbitlosedThisquesClosedyearsagotionisofftopicItisnotcurrentlyacceptinganswersWanttoimprovethisquestionUpdatethequestionsoitsontopicforInformationSecurityStackExchangen";

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public Long getUserId(String token) {
		return Long.valueOf(getClaimFromToken(token, Claims::getSubject));
	}

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

	public String generateRefreshToken(Long userId) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, userId);
	}

	private String doGenerateToken(Map<String, Object> claims, Long userId) {

		return Jwts.builder().setClaims(claims).setSubject(String.valueOf(userId))
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.HS512, secret).compact();
	}

	public Boolean validateToken(String refreshToken) {
		return isTokenExpired(refreshToken);
	}
}
