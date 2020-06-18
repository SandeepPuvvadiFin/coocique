package com.ofss.digx.cz.ccq.appx.framework.session;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ofss.digx.app.context.ChannelContext;
import com.ofss.digx.app.core.ChannelInteraction;
import com.ofss.digx.appx.AbstractRESTApplication;
import com.ofss.digx.cz.ccq.app.session.dto.SolicitarResponseDTO;
import com.ofss.digx.cz.ccq.security.provider.SolicitarProvider;
import com.ofss.digx.infra.exceptions.Exception;
import com.ofss.fc.infra.log.impl.MultiEntityLogger;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

@Path("/authentication")
public class Authentication extends AbstractRESTApplication {
	/**
	 * Stores the name of the entity(class) represented by this {@code Class} object
	 * as a {@code String}.
	 */
	private static final String THIS_COMPONENT_NAME = Authentication.class.getName();

	/**
	 * Create instance of multi-entity logger.
	 */
	private static final MultiEntityLogger FORMATTER = MultiEntityLogger.getUniqueInstance();
	/**
	 * This is an instance variable which is required to support multi-entity wide
	 * logging.
	 */
	private static final Logger LOGGER = FORMATTER.getLogger(THIS_COMPONENT_NAME);

	/**
	 * Public constructor.
	 */
	public Authentication() {

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{digitalID}")
	public Response create(
			@Parameter(in = ParameterIn.PATH, required = true, name = "digitalID", description = "Digital id of user", schema = @Schema(type = "String")) @PathParam("digitalID") String digitalID) {

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE,
					FORMATTER.formatMessage("Entered into create of authenticationServices REST Service."));
		}
		ChannelContext channelContext = null;
		ChannelInteraction channelInteraction = null;
		Response response = null;
		try {
			channelContext = super.getChannelContext();
			channelInteraction = ChannelInteraction.getInstance();
			SolicitarProvider solPrivader = new SolicitarProvider();
			SolicitarResponseDTO responseSol = solPrivader.GenerateCode(digitalID);
			response = buildResponse(responseSol, Response.Status.OK);

		} catch (java.lang.Exception e) {
			LOGGER.log(Level.SEVERE, FORMATTER.formatMessage(
					"Exception encountered while invoking the core service create for authentication. digital id =%s",
					digitalID), e);
			response = buildResponse(new Exception(e), Response.Status.BAD_REQUEST);
		} finally {
			try {
				channelInteraction.close(channelContext);
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						FORMATTER.formatMessage("Error encountered while closing channelContext %s", channelContext),
						e);
				response = buildResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
			}
		}

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.log(Level.FINE, "Exiting from Session Create()");
		}

		return response;
	}

}
