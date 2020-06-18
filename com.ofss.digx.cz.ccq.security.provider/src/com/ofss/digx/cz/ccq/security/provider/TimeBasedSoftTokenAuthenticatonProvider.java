package com.ofss.digx.cz.ccq.security.provider;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.Subject;

import com.ofss.digx.app.dto.authentication.AuthenticationInfoDTO;
import com.ofss.digx.app.dto.authentication.challenge.ChallengeResponse;
import com.ofss.digx.app.dto.authentication.challenge.TimeBasedTokenChallenge;
import com.ofss.digx.app.dto.authentication.challenge.TimeBasedTokenChallengeResponse;
import com.ofss.digx.app.dto.user.UserAppDataDTO;
import com.ofss.digx.app.security.exceptions.tfa.TFARequiredException;
import com.ofss.digx.app.security.exceptions.tfa.TFAValidationFailedException;
import com.ofss.digx.core.adapter.AdapterFactory;
import com.ofss.digx.core.adapter.subject.ISubjectAdapter;
import com.ofss.digx.cz.ccq.framework.security.session.entity.CZUserSession;
import com.ofss.digx.cz.ccq.framework.security.session.entity.CZUserSessionKey;
import com.ofss.digx.domain.security.entity.authentication.AuthenticationInfo;
import com.ofss.digx.enumeration.authentication.TOTPKeyRepresentationType;
import com.ofss.digx.framework.security.authentication.helper.TokenExpiryFactory;
import com.ofss.digx.framework.security.authentication.provider.I2FactorAuthenticationProvider;
import com.ofss.digx.framework.security.authentication.provider.constraints.SoftTokenSystemConstraint;
import com.ofss.digx.framework.security.entity.authentication.challenge.TimeBasedToken;
import com.ofss.digx.framework.security.entity.authentication.challenge.TimeBasedTokenKey;
import com.ofss.digx.infra.exceptions.Exception;
import com.ofss.digx.infra.exceptions.TOTPAuthenticatorException;
import com.ofss.digx.infra.thread.ThreadAttribute;
import com.ofss.digx.security.core.evaluator.authndata.TwoFactorAuthenticationData;
import com.ofss.fc.datatype.Date;
import com.ofss.fc.infra.config.ConfigurationFactory;
import com.ofss.fc.infra.log.impl.MultiEntityLogger;
import com.ofss.fc.infra.validation.error.ValidationError ;

import oracle.security.jps.util.SubjectUtil;

public class TimeBasedSoftTokenAuthenticatonProvider implements I2FactorAuthenticationProvider {
	private static final String THIS_COMPONENT_NAME = TimeBasedSoftTokenAuthenticatonProvider.class.getName();
	private transient Logger logger;
	private MultiEntityLogger formatter;
	private static String tokenExpiryClass = null;
	private long timeStepSizeInMillis;
	private int windowSize;
	private int codeDigits;
	private int keyModulus;
	private TOTPKeyRepresentationType keyRepresentation;
	private static final String HMAC_HASH_FUNCTION = "HmacSHA1";
	public static final String AUTHENTICATION_TYPE = "T_SOFT_TOKEN";
	private static Preferences preferences = ConfigurationFactory.getInstance()
			.getConfigurations("authenticationConfig");

	public TimeBasedSoftTokenAuthenticatonProvider() {
		this.logger = MultiEntityLogger.getUniqueInstance().getLogger(THIS_COMPONENT_NAME);

		this.formatter = MultiEntityLogger.getUniqueInstance();

		this.timeStepSizeInMillis = TimeUnit.SECONDS.toMillis(30L);

		this.windowSize = 6;

		this.codeDigits = 6;

		this.keyModulus = (int) Math.pow(10.0D, (double) this.codeDigits);

		this.keyRepresentation = TOTPKeyRepresentationType.BASE64;
	}

	public Date generate(AuthenticationInfo authInfo, TwoFactorAuthenticationData authenticationData,
			String referenceNumber) throws TFARequiredException {
		TimeBasedToken timeBasedToken = null;
		TimeBasedTokenKey key = null;

		SoftTokenSystemConstraint softTokenConstraint = new SoftTokenSystemConstraint(authInfo);
		softTokenConstraint.isSatisfiedBy();

		try {
			Date currentTime = new Date();
			timeBasedToken = new TimeBasedToken();
			key = new TimeBasedTokenKey();
			key.setReferenceNo(referenceNumber);
			timeBasedToken.setKey(key);
			timeBasedToken.setCreationDate(currentTime);
			timeBasedToken.create(timeBasedToken);
			TimeBasedTokenChallenge timeBasedTokenRequestDTO = new TimeBasedTokenChallenge();
			timeBasedTokenRequestDTO.setReferenceNo(referenceNumber);
			ThreadAttribute.set("X-CHALLENGE", timeBasedTokenRequestDTO);

			return timeBasedToken.getCreationDate();
		} catch (Exception var9) {
			this.logger.log(Level.SEVERE, this.formatter.formatMessage("Cannot save time based token", new Object[0]),
					var9);
			throw new TOTPAuthenticatorException("The operation cannot be performed now.");
		}
	}

	public Boolean validate(TwoFactorAuthenticationData authenticationData, String referenceNumber,
			ChallengeResponse response) throws TFAValidationFailedException {
		TimeBasedToken timeBasedToken = new TimeBasedToken();
		TimeBasedTokenKey key = new TimeBasedTokenKey();
		key.setReferenceNo(referenceNumber);
		TimeBasedTokenChallengeResponse responseDTO = (TimeBasedTokenChallengeResponse) response;
		String hashedToken = responseDTO.getTOTP();
		try {
			timeBasedToken = timeBasedToken.read(key);

			ISubjectAdapter subjectAdapter = (ISubjectAdapter) AdapterFactory.getInstance()
					.getAdapter(ISubjectAdapter.class);
			String username = (String) com.ofss.fc.infra.thread.ThreadAttribute.get("SUBJECT_NAME");
			UserAppDataDTO userAppDataDTO = subjectAdapter.getUserAppData(username);
			String sharedSecret = userAppDataDTO.getOtpSeed();

			if (this.timeValidation(timeBasedToken.getCreationDate(), AUTHENTICATION_TYPE)) {
				long utcTime = this.getUTCTime(timeBasedToken.getCreationDate());
				if (hashedToken != null && !hashedToken.isEmpty() && sharedSecret != null && !sharedSecret.isEmpty()
						&& this.validateToken(sharedSecret, Long.parseLong(hashedToken), utcTime)) {

					timeBasedToken.delete(timeBasedToken);
					return true;
				} else {
					throw new TFAValidationFailedException("DIGX_TFA_0004");
				}
			} else {
				timeBasedToken.delete(timeBasedToken);
				throw new TFAValidationFailedException("DIGX_TFA_0005");
			}
		} catch (TFAValidationFailedException var14) {
			this.updateChallengeRequestDTO(referenceNumber);
			throw var14;
		} catch (Exception var15) {
			this.updateChallengeRequestDTO(referenceNumber);
			this.logger.log(Level.SEVERE,
					this.formatter.formatMessage("Exception while retreiving token", new Object[0]), var15);
			throw new TFAValidationFailedException("DIGX_AUTH_0010");
		} catch (NumberFormatException var16) {
			this.updateChallengeRequestDTO(referenceNumber);
			this.logger.log(Level.SEVERE,
					this.formatter.formatMessage("Exception while retreiving token", new Object[0]), var16);
			throw new TFAValidationFailedException("DIGX_AUTH_0010");
		} catch (ClassNotFoundException var17) {
			this.updateChallengeRequestDTO(referenceNumber);
			this.logger.log(Level.SEVERE,
					this.formatter.formatMessage("Exception while retreiving token", new Object[0]), var17);
			throw new TFAValidationFailedException("DIGX_AUTH_0010");
		} catch (TOTPAuthenticatorException var18) {
			this.updateChallengeRequestDTO(referenceNumber);
			this.logger.log(Level.SEVERE,
					this.formatter.formatMessage("Exception while retreiving token", new Object[0]), var18);
			throw new TFAValidationFailedException("DIGX_AUTH_0010");
		}
	}

	private void updateChallengeRequestDTO(String referenceNumber) {
		TimeBasedTokenChallenge requestDTO = new TimeBasedTokenChallenge();
		requestDTO.setReferenceNo(referenceNumber);
		ThreadAttribute.set("X-CHALLENGE", requestDTO);

	}

	public Date resend(TwoFactorAuthenticationData authenticationData, String referenceNumber) {
		return null;
	}

	private boolean checkCode(String secret, long code, long timestamp, int window) {
		byte[] decodedKey;
		switch (keyRepresentation) {
		case BASE64:
			decodedKey = Base64.getDecoder().decode(secret);
			break;
		default:
			logger.log(Level.SEVERE, formatter.formatMessage("Unknown key representation type."));
			throw new IllegalArgumentException("Unknown key representation type.");
		}
		final long timeWindow = timestamp / timeStepSizeInMillis;
		for (int i = 0; i < window; i++) {
			long hash = calculateCode(decodedKey, timeWindow + i);
			if (hash == code) {
				return true;
			}
		}
		return false;
	}

	private int calculateCode(byte[] key, long time) {
		byte[] data = new byte[8];
		long value = time;
		for (int i = 8; i-- > 0; value >>>= 8) {
			data[i] = (byte) ((int) value);
		}
		SecretKeySpec signKey = new SecretKeySpec(key, HMAC_HASH_FUNCTION);
		try {
			Mac mac = Mac.getInstance(HMAC_HASH_FUNCTION);
			mac.init(signKey);
			byte[] hash = mac.doFinal(data);
			int offset = hash[hash.length - 1] & 15;
			long truncatedHash = 0L;
			for (int i = 0; i < 4; ++i) {
				truncatedHash <<= 8;
				truncatedHash |= (long) (hash[offset + i] & 255);
			}
			truncatedHash &= 2147483647L;
			truncatedHash %= (long) this.keyModulus;
			return (int) truncatedHash;
		} catch (NoSuchAlgorithmException var14) {
			this.logger.log(Level.SEVERE,
					this.formatter.formatMessage("Exception in instantiating the custom class creation", new Object[0]),
					var14);

			throw new TOTPAuthenticatorException("The operation cannot be performed now.");
		} catch (InvalidKeyException var15) {
			this.logger.log(Level.SEVERE,
					this.formatter.formatMessage("Exception in instantiating the custom class creation", new Object[0]),
					var15);

			throw new TOTPAuthenticatorException("The operation cannot be performed now.");
		}
	}

	private boolean validateToken(String secret, long verificationCode, long time) throws TOTPAuthenticatorException {
		if (secret == null) {
			this.logger.log(Level.SEVERE, this.formatter.formatMessage("Secret key provided is null.", new Object[0]));
			throw new IllegalArgumentException("Secret key provided is null.");
		}
		if (verificationCode <= 0 || verificationCode >= keyModulus) {
			return false;
		}

		windowSize = preferences.getInt("TOKEN_TIME_WINDOWS_ALLOWED", windowSize);

		return checkCode(secret, verificationCode, time, windowSize);
	}

	private long getUTCTime(Date localDate) {
		ZonedDateTime utc = Instant.ofEpochMilli(localDate.getMillis()).atZone(ZoneOffset.UTC);
		return utc.toInstant().toEpochMilli();
	}

	public ValidationError validateParams(String userSegmentId, AuthenticationInfoDTO authenticationInfoDTO) {
		return null;
	}

	private boolean timeValidation(Date creationTime, String authType) throws ClassNotFoundException {
		TokenExpiryFactory expiryFactory = TokenExpiryFactory.getInstance();

		long expiryTimeMillis = expiryFactory.getExpiryTimeCalculator(tokenExpiryClass).getExpiryTime(authType);

		Date expiryTime = new Date(creationTime.getMillis() + expiryTimeMillis * (long) this.windowSize);

		return expiryTime != null && expiryTime.isAfterOrEqual(new Date());
	}

	public Boolean of(String authTypeId) {
		return authTypeId.equals(AUTHENTICATION_TYPE) ? true : false;
	}

	public Boolean isCustom() {
		return false;
	}

	public CZUserSession getSessionDetail() {
		Subject subject = SubjectUtil.getCurrentSubject();
		String userName = SubjectUtil.getUserName(subject);
		String sessionId = (String) com.ofss.fc.infra.thread.ThreadAttribute
				.get(com.ofss.fc.infra.thread.ThreadAttribute.SESSION_ID);
		CZUserSessionKey key = new CZUserSessionKey();
		key.setSessionId(sessionId);
		key.setUserName(userName);
		CZUserSession czSession = new CZUserSession();
		czSession.setKey(key);
		try {
			return czSession.read(key);
		} catch (Exception e) {
			return null;
		}

	}

}