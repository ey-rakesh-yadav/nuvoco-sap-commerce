package com.nuvoco.security;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.search.restriction.SearchRestrictionService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RequiredRoleMatchingFilter extends OncePerRequestFilter {

	private static final String ROLE_PREFIX = "ROLE_";
	private String regexp;
	private static final Logger LOG = Logger.getLogger(RequiredRoleMatchingFilter.class);

	@Resource
	private BaseSiteService baseSiteService;

	private SessionService sessionService;


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		final Authentication auth = getAuth();
		boolean isCustomer = false;
		if (hasRole(NuvocoSecuredAccessConstants.ROLE_CUSTOMERGROUP, auth) || hasRole(NuvocoSecuredAccessConstants.ROLE_CUSTOMERMANAGERGROUP, auth)) {
			isCustomer = true;
		}

		boolean isAllowed = false;
		final Optional<BaseSiteModel> currentBaseSite = Optional.ofNullable(baseSiteService.getCurrentBaseSite());
		if (isCustomer && currentBaseSite.isPresent() && currentBaseSite.get().getAllowedUnit() != null) {
			List<B2BUnitModel> allowedUnit = currentBaseSite.get().getAllowedUnit();
			for(B2BUnitModel b2BUnitModel : allowedUnit){
				final String REQUIRED_ROLE = ROLE_PREFIX + b2BUnitModel.getUid().toUpperCase();
				if (hasRole(REQUIRED_ROLE, auth)) {
					LOG.debug("Authorized as " + b2BUnitModel.getUid());
					isAllowed = true;
					break;
				}
			}
			if(!isAllowed){
				throw new AccessDeniedException("Access is denied , User Does not belong to current site");
			}

		}


		filterChain.doFilter(request, response);
	}

	protected Authentication getAuth()
	{
		return SecurityContextHolder.getContext().getAuthentication();
	}

	protected boolean hasRole(final String role, final Authentication auth)
	{
		if (auth != null)
		{
			for (final GrantedAuthority ga : auth.getAuthorities())
			{
				if (ga.getAuthority().equals(role))
				{
					return true;
				}
			}
		}
		return false;
	}


	public SessionService getSessionService() {
		return sessionService;
	}

	public void setSessionService(SessionService sessionService) {
		this.sessionService = sessionService;
 	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public static final String BASE_SITES_ENDPOINT_PATH = "/basesites";

	protected boolean matchesUrl(final HttpServletRequest request, final String regexp)
	{
		final Matcher matcher = getMatcher(request, regexp);
		return matcher.find();
	}

	protected String getBaseSiteValue(final HttpServletRequest request, final String regexp)
	{
		if (BASE_SITES_ENDPOINT_PATH.equals(getPath(request)))
		{
			return null;
		}

		final Matcher matcher = getMatcher(request, regexp);
		if (matcher.find())
		{
			return matcher.group().substring(1);
		}
		return null;
	}

	protected String getValue(final HttpServletRequest request, final String regexp)
	{
		final Matcher matcher = getMatcher(request, regexp);
		if (matcher.find())
		{
			return matcher.group(1);
		}
		return null;
	}

	protected String getValue(final HttpServletRequest request, final String regexp, final String groupName)
	{
		final Matcher matcher = getMatcher(request, regexp);
		if (matcher.find())
		{
			return matcher.group(groupName);
		}
		return null;
	}

	protected Matcher getMatcher(final HttpServletRequest request, final String regexp)
	{
		final Pattern pattern = Pattern.compile(regexp);
		final String path = getPath(request);
		return pattern.matcher(path);
	}

	protected String getPath(final HttpServletRequest request)
	{
		return StringUtils.defaultString(request.getPathInfo());
	}

	protected String updateStringValueFromRequest(final HttpServletRequest request, final String paramName,
			final String defaultValue)
	{
		final String requestParameterValue = getRequestParameterValue(request, paramName);
		if ("".equals(requestParameterValue))
		{
			return null;
		}
		return StringUtils.defaultIfBlank(requestParameterValue, defaultValue);
	}
	
	protected String getRequestParameterValue(final HttpServletRequest request, final String paramName)
	{
		return request.getParameter(paramName);
	}
}


