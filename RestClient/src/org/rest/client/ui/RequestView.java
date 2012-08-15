/*******************************************************************************
 * Copyright 2012 Paweł Psztyć
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.rest.client.ui;


import java.util.ArrayList;

import org.rest.client.request.FilesObject;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

public interface RequestView extends IsWidget {
	/**
	 * Presenter interface
	 * @author Paweł Psztyć
	 *
	 */
	public interface Presenter {
		/**
		 * Change place
		 * @param place
		 */
		void goTo(Place place);
		/**
		 * Request app to open Add Encoding Dialog.
		 * @param previousEncoding Previously selected encoding
		 */
		void requestAddEncodingDialog(String previousEncoding);
		/**
		 * Fire event for clear form action
		 */
		void fireClearAllEvent();
	}
	/**
	 * Sets presenter for this view
	 * @param listener
	 */
	void setPresenter(Presenter listener);
	
	/**
	 * Accessors for form data
	 */
	String getUrl();
	String getMethod();
	String getHeaders();
	String getPayload();
	String getEncoding();
	ArrayList<FilesObject> getFiles();
	
	void setUrl(String url);
	void setMethod(String method);
	void setHeaders(String header);
	void setPayload(String payload);
	void setEncoding(String encoding);
	/**
	 * Append user defined encoding list to defult list
	 * @param values
	 */
	void appendEncodingValues(String[] values);
}