/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder.sitemonitor.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ngrinder.infra.spring.RemainedPath;
import org.ngrinder.model.User;
import org.ngrinder.script.controller.FileEntryController;
import org.ngrinder.script.model.FileEntry;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * FileEntry logic is management to SVN repository. store name created by User.getUserid.
 * This class is use the store name to "USERID + USERID_SUFFIX" for Sitemonitor.
 * 
 * @author Gisoo Gwon
 */
@Controller
@RequestMapping("/sitemonitor/script")
public class SitemonitorFileEntryController extends FileEntryController {

	// Used href value of <a> tag.
	static final String URL_PREFIX = "/sitemonitor";
	// Used to create sitemonitor svn store name.
	private static final String USERID_SUFFIX = "-sitemonitor";

	private User cloneAndChangeUserId(User user) {
		User cloneUser = new User(
			user.getUserId() + USERID_SUFFIX,
			user.getUserName(),
			user.getPassword(),
			user.getEmail(),
			user.getRole()
			);
		return cloneUser;
	}
	
	/**
	 * If viewName is redirect url, add the URL_PREFIX after "redirect:" 
	 * @param viewName
	 * @return If not redirect url, return viewName.<br>
	 * but redirect url is return "redirect:URL_PREFIX/viewName" 
	 */
	String replaceRedirectUrl(String viewName) {
		if (!viewName.contains("redirect:")) {
			return viewName;
		}

		String url = viewName.replace("redirect:", "");
		boolean absoluteUrl = url.startsWith("/");
		if (absoluteUrl) {
			return "redirect:" + URL_PREFIX + url;
		} else {
			return "redirect:" + URL_PREFIX.replace("/", "") + "/" + url;
		}
	}
	
	@Override
	public String getAll(User user, final @RemainedPath String path, ModelMap model) {
		model.addAttribute("urlPrefix", URL_PREFIX);
		String viewName = super.getAll(cloneAndChangeUserId(user), path, model);
		return replaceRedirectUrl(viewName);
	}

	@Override
	public String addFolder(User user, @RemainedPath String path,
			@RequestParam("folderName") String folderName, ModelMap model) {
		model.addAttribute("urlPrefix", URL_PREFIX);
		String viewName = super.addFolder(cloneAndChangeUserId(user), path, folderName, model);
		return replaceRedirectUrl(viewName);
	}

	@Override
	public String createForm(
			User user,
			@RemainedPath String path,
			@RequestParam(value = "testUrl", required = false) String testUrl,
			@RequestParam("fileName") String fileName,
			@RequestParam(value = "scriptType", required = false) String scriptType,
			@RequestParam(value = "createLibAndResource", defaultValue = "false") boolean createLibAndResources,
			RedirectAttributes redirectAttributes, ModelMap model) {
		model.addAttribute("urlPrefix", URL_PREFIX);
		String viewName = super.createForm(cloneAndChangeUserId(user), path, testUrl, fileName, scriptType,
			createLibAndResources, redirectAttributes, model);
		return replaceRedirectUrl(viewName);
	}

	@Override
	public String getOne(User user, @RemainedPath String path,
			@RequestParam(value = "r", required = false) Long revision, ModelMap model) {
		model.addAttribute("urlPrefix", URL_PREFIX);
		String viewName = super.getOne(cloneAndChangeUserId(user), path, revision, model);
		model.addAttribute("ownerId", user.getUserId());
		return replaceRedirectUrl(viewName);
	}

	@Override
	public void download(User user, @RemainedPath String path, HttpServletResponse response) {
		super.download(cloneAndChangeUserId(user), path, response);
	}

	@Override
	public String search(User user,
			@RequestParam(required = true, value = "query") final String query, ModelMap model) {
		model.addAttribute("urlPrefix", URL_PREFIX);
		String viewName = super.search(cloneAndChangeUserId(user), query, model);
		return replaceRedirectUrl(viewName);
	}

	@Override
	public String save(User user, FileEntry fileEntry, @RequestParam String targetHosts,
			@RequestParam(defaultValue = "0") String validated,
			@RequestParam(defaultValue = "false") boolean createLibAndResource, ModelMap model) {
		model.addAttribute("urlPrefix", URL_PREFIX);
		String viewName = super.save(cloneAndChangeUserId(user), fileEntry, targetHosts, validated,
			createLibAndResource, model);
		return replaceRedirectUrl(viewName);
	}

	@Override
	public String upload(User user, @RemainedPath String path,
			@RequestParam("description") String description,
			@RequestParam("uploadFile") MultipartFile file, ModelMap model) {
		model.addAttribute("urlPrefix", URL_PREFIX);
		String viewName = super.upload(cloneAndChangeUserId(user), path, description, file, model);
		return replaceRedirectUrl(viewName);
	}

	@Override
	public String delete(User user, @RemainedPath String path,
			@RequestParam("filesString") String filesString) {
		String viewName = super.delete(cloneAndChangeUserId(user), path, filesString);
		return replaceRedirectUrl(viewName);
	}

	@Override
	public HttpEntity<String> create(User user, FileEntry fileEntry) {
		return super.create(cloneAndChangeUserId(user), fileEntry);
	}

	@Override
	public HttpEntity<String> uploadForAPI(User user, @RemainedPath String path,
			@RequestParam("description") String description,
			@RequestParam("uploadFile") MultipartFile file) throws IOException {
		return super.uploadForAPI(cloneAndChangeUserId(user), path, description, file);
	}

	@Override
	public HttpEntity<String> viewOne(User user, @RemainedPath String path) {
		return super.viewOne(cloneAndChangeUserId(user), path);
	}

	@Override
	public HttpEntity<String> getAll(User user) {
		return super.getAll(cloneAndChangeUserId(user));
	}

	@Override
	public HttpEntity<String> getAll(User user, @RemainedPath String path) {
		return super.getAll(cloneAndChangeUserId(user), path);
	}

	@Override
	public HttpEntity<String> deleteOne(User user, @RemainedPath String path) {
		return super.deleteOne(cloneAndChangeUserId(user), path);
	}

	@Override
	public HttpEntity<String> validate(User user, FileEntry fileEntry,
			@RequestParam(value = "hostString", required = false) String hostString) {
		return super.validate(cloneAndChangeUserId(user), fileEntry, hostString);
	}
	
	/**
	 * Get the SVN url BreadCrumbs HTML string.
	 *
	 * @param user user
	 * @param path path
	 * @return generated HTML
	 */
	@Override
	public String getSvnUrlBreadcrumbs(User user, String path) {
		String contextPath = httpContainerContext.getCurrentContextUrlFromUserRequest();
		String[] parts = StringUtils.split(path, '/');
		StringBuilder accumulatedPart = new StringBuilder(contextPath).append(URL_PREFIX + "/script/list");
		StringBuilder returnHtml = new StringBuilder().append("<a href='").append(accumulatedPart).append("'>")
				.append(contextPath).append("/svn/").append(user.getUserId()).append("</a>");
		for (String each : parts) {
			returnHtml.append("/");
			accumulatedPart.append("/").append(each);
			returnHtml.append("<a href='").append(accumulatedPart).append("'>").append(each).append("</a>");
		}
		return returnHtml.toString();
	}


	/**
	 * Get the script path BreadCrumbs HTML string.
	 *
	 * @param path path
	 * @return generated HTML
	 */
	@Override
	public String getScriptPathBreadcrumbs(String path) {
		String contextPath = httpContainerContext.getCurrentContextUrlFromUserRequest();
		String[] parts = StringUtils.split(path, '/');
		StringBuilder accumulatedPart = new StringBuilder(contextPath).append(URL_PREFIX + "/script/list");
		StringBuilder returnHtml = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			String each = parts[i];
			accumulatedPart.append("/").append(each);
			if (i != parts.length - 1) {
				returnHtml.append("<a target='_path_view' href='").append(accumulatedPart).append("'>").append(each)
						.append("</a>").append("/");
			} else {
				returnHtml.append(each);
			}
		}
		return returnHtml.toString();
	}

}
