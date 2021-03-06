package cs5412.project.distributed_file_system.service.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cs5412.project.distributed_file_system.service.CookieService;
import cs5412.project.distributed_file_system.service.Sha1HashService;

@Named
public class CookieServiceImpl implements CookieService {

	private static final String UID = "UidBySW773";
	private static final String UID_VERIFICATION = "UidVerificationBySW773";
	private static final String UID_HASHSALT = "oh yeah~";
	private static final String FID = "FidBySW773";
	private static final String BRANCH_ID = "BidBySW773";
	private static final String BRANCH_NAME = "BranchNameBySW773";
	private static final int COOKIE_TIMEOUT = 120;

	@Override
	public void storeFolderFid(int fid, HttpServletResponse response) {
		Cookie cookie = new Cookie(FID, Integer.toString(fid));
		cookie.setMaxAge(COOKIE_TIMEOUT);
		response.addCookie(cookie);
	}

	@Override
	public int getFolderFid(HttpServletRequest request) {
		String fidStr = this.getCookieVal(FID, request);
		if (fidStr != null) {
			return Integer.parseInt(fidStr);
		}
		return -1;
	}

	private String getCookieVal(String cookieName, HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String name = cookie.getName();
				String value = cookie.getValue();
				if (name.equalsIgnoreCase(cookieName)) {
					return value;
				}
			}
		}
		return null;
	}

	@Override
	public void storeBranchInfo(int fid, String branchName,
			HttpServletResponse response) {
		Cookie cookie = new Cookie(BRANCH_ID, Integer.toString(fid));
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);

		cookie = new Cookie(BRANCH_NAME, branchName);
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);

	}

	@Override
	public int getBranchFid(HttpServletRequest request) {
		String fidStr = this.getCookieVal(BRANCH_ID, request);
		if (fidStr != null) {
			return Integer.parseInt(fidStr);
		}
		return -1;
	}

	@Override
	public String getBranchName(HttpServletRequest request) {
		String branchName = this.getCookieVal(BRANCH_NAME, request);
		if (branchName != null) {
			return branchName;
		}
		return "";
	}

	@Override
	public void storeUid(int uid, HttpServletResponse response) {
		Cookie cookie = new Cookie(UID, Integer.toString(uid));
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);

		String verificationHash = Sha1HashService.sha1(Integer.toString(uid)
				+ UID_HASHSALT);
		cookie = new Cookie(UID_VERIFICATION, verificationHash);
		cookie.setMaxAge(Integer.MAX_VALUE);
		response.addCookie(cookie);
	}

	@Override
	public int getUidFromCookie(HttpServletRequest request) {
		String uidStr = this.getCookieVal(UID, request);
		if (uidStr != null) {
			return Integer.parseInt(uidStr);
		}
		return -1;
	}

	@Override
	public boolean verifyUid(int uid, HttpServletRequest request) {
		String storedHash = this.getCookieVal(UID_VERIFICATION, request);
		if (storedHash == null) {
			return false;
		}
		String computedHash = Sha1HashService.sha1(Integer.toString(uid)
				+ UID_HASHSALT);
		if (computedHash.equals(storedHash)) {
			return true;
		} else {
			return false;
		}
	}

}
