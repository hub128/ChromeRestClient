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
package org.rest.client;

import java.util.Date;
import java.util.HashMap;

import org.rest.client.event.AddEncodingEvent;
import org.rest.client.event.ApplicationReadyEvent;
import org.rest.client.event.ClearFormEvent;
import org.rest.client.event.ClearHistoryEvent;
import org.rest.client.event.CustomEvent;
import org.rest.client.event.HttpMethodChangeEvent;
import org.rest.client.event.RequestStartActionEvent;
import org.rest.client.event.RequestStopEvent;
import org.rest.client.event.URLFieldToggleEvent;
import org.rest.client.event.UrlValueChangeEvent;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.web.bindery.event.shared.EventBus;

/**
 * This class is needed to register application events handlers and fire it on
 * browsers document object so external plugins can take action when other
 * action occur.
 * 
 * <p>
 * To handle applications events you need to register an handler for each type
 * of event you need to handle. <br/>
 * For example:
 * <pre>
 * document.addEventListener('arc:EVENT_NAME', function(e){ //do something... });
 * </pre>
 * For list of all custom events check {@link CustomEvent} enum.
 * </p>
 * 
 * @author Paweł Psztyć
 * 
 */
public class ExternalEventsFactory {
	
//	public final static String EXT_OBSERVE_COOKIE = "observeCookie";
//	public final static String EXT_STOP_OBSERVE_COOKIE = "stopObserveCookie";
	public final static String EXT_REQUEST_BEGIN = "requestBegin";
	public final static String EXT_GET_COLLECTED_REQUEST_DATA = "getRequestData";
	public final static String EXT_GET_EXTERNAL_REQUEST_DATA = "getExternalData";
	
	interface MessageReceivedHandler {
		/**
		 * Passed message must always have "payload" string and can have some data
		 * @param payload
		 * @param message
		 */
		void onMessage(String payload, String message);
	}
	
	private static HashMap<String, Callback<String, Throwable>> messageCallbackList = new HashMap<String, Callback<String,Throwable>>();
	
	/**
	 * Register handlers for all custom events available for application.
	 * @param eventBus
	 */
	public static void init(EventBus eventBus) {
		
		//
		// Register application's actions handlers
		//
		
		ApplicationReadyEvent.register(eventBus, new ApplicationReadyEvent.Handler() {
			@Override
			public void onReady() {
				fireDocumentEvent(CustomEvent.APPLICATION_READY.getValue());
			}
		});
		AddEncodingEvent.register(eventBus, new AddEncodingEvent.Handler() {
			@Override
			public void onAddEncoding(String encoding) {
				fireDocumentEvent(CustomEvent.ADD_ENCODING.getValue(), encoding);
			}
		});
		UrlValueChangeEvent.register(eventBus, new UrlValueChangeEvent.Handler() {
			@Override
			public void onUrlChange(String url) {
				fireDocumentEvent(CustomEvent.URL_CHANGE.getValue(), url);
			}
		});
		RequestStartActionEvent.register(eventBus, new RequestStartActionEvent.Handler() {
			@Override
			public void onStart(Date time) {
				double startTime = (double) time.getTime();
				fireDocumentEvent(CustomEvent.REQUEST_START_ACTION.getValue(), startTime);
			}
		});
		RequestStopEvent.register(eventBus, new RequestStopEvent.Handler() {
			@Override
			public void onStop(Date time) {
				double endTime = (double) time.getTime();
				fireDocumentEvent(CustomEvent.REQUEST_STOP.getValue(), endTime);
			}
		});
		
		
		
		
		//
		// Register application's UI state change handlers
		//
		
		ClearFormEvent.register(eventBus, new ClearFormEvent.Handler() {
			@Override
			public void onClearForm() {
				fireDocumentEvent(CustomEvent.CLEAR_ALL.getValue());
			}
		});
		URLFieldToggleEvent.register(eventBus, new URLFieldToggleEvent.Handler() {
			@Override
			public void onClearForm(boolean isNowSimpleView) {
				String value = "simple";
				if(!isNowSimpleView){
					value = "detailed";
				}
				fireDocumentEvent(CustomEvent.URL_FIELD_TOGGLE.getValue(), value);
			}
		});
		HttpMethodChangeEvent.register(eventBus, new HttpMethodChangeEvent.Handler() {
			@Override
			public void onMethodChange(String method) {
				fireDocumentEvent(CustomEvent.HTTP_METHOD_CHANGE.getValue(), method);
			}
		});
		ClearHistoryEvent.register(eventBus, new ClearHistoryEvent.Handler() {
			@Override
			public void onClearForm() {
				fireDocumentEvent(CustomEvent.CLEAR_HISTORY.getValue());
			}
		});
		
		registerMessageHandler(new MessageReceivedHandler() {
			@Override
			public void onMessage(String payload, String message) {
				JSONObject respObj = null;
				if(payload.equals("ping")){
					respObj = preparePostMessage();
					respObj.put("data", new JSONString("ok"));
					respObj.put("payload", new JSONString("ping"));
					sendExtensionMessage(respObj.toString());
				} else {
					if(messageCallbackList.containsKey(payload)){
						messageCallbackList.get(payload).onSuccess(message);
						messageCallbackList.remove(payload);
					}
				}
			}
		});
	}
	/**
	 * Send message to background page via extension's message passing system.
	 * @param payload Payload string
	 * @param data any data to pass
	 */
	public static final void postMessage(String payload, String data){
		final JSONObject respObj = preparePostMessage();
		if(data != null){
			respObj.put("data", new JSONString(data));
		}
		respObj.put("payload", new JSONString(payload));
		sendExtensionMessage(respObj.toString());
	}
	/**
	 * Send message to background page via extension's message passing system.
	 * @param payload Payload string
	 * @param data any data to pass
	 */
	public static final void postMessage(String payload, String data, Callback<String, Throwable> callback){
		messageCallbackList.put(payload, callback);
		
		final JSONObject respObj = preparePostMessage();
		if(data != null){
			respObj.put("data", new JSONString(data));
		}
		respObj.put("payload", new JSONString(payload));
		sendExtensionMessage(respObj.toString());
	}
	
	
	private final static native void sendExtensionMessage(String respObj)/*-{
		$wnd.postMessage(respObj, $wnd.location.href);
	}-*/;


	private final static native void registerMessageHandler(MessageReceivedHandler handler)/*-{
		var rec = $entry(function(e) {
			if(e.origin != location.origin){ return; };
			if(!(e.data && e.data.source && e.data.source == "arc:cs")) return;
			if(!(e.data && e.data.payload)) return;
			//console.log('e.data', e.data);
			handler.@org.rest.client.ExternalEventsFactory.MessageReceivedHandler::onMessage(Ljava/lang/String;Ljava/lang/String;)(e.data.payload,e.data.data);
		});
		$wnd.addEventListener("message", rec, false);
		var e = $doc.createEvent('Events');
		e.initEvent('ARC:READY');
		$wnd.dispatchEvent(e);
	}-*/;
	
	
	private static JSONObject preparePostMessage(){
		JSONObject respObj = new JSONObject();
		respObj.put("source", new JSONString("arc:gwt"));
		return respObj;
	}
	
	private final static native void fireDocumentEvent(String eventName) /*-{
		var e = $doc.createEvent('Events');
		e.initEvent(eventName);
		$doc.dispatchEvent(e);
	}-*/;
	
	private final static native void fireDocumentEvent(String eventName,
			JavaScriptObject passedData) /*-{
		var e = $doc.createEvent('Events');
		e.initEvent(eventName);
		if(passedData){
			e.data = passedData;
		}
		$doc.dispatchEvent(e);
	}-*/;
	
	private final static native void fireDocumentEvent(String eventName,
			double passedData) /*-{
		var e = $doc.createEvent('Events');
		e.initEvent(eventName);
		if(passedData){
			e.data = passedData;
		}
		$doc.dispatchEvent(e);
	}-*/;
	
	private final static native void fireDocumentEvent(String eventName,
			String passedData) /*-{
		var e = $doc.createEvent('Events');
		e.initEvent(eventName);
		if(passedData){
			e.data = passedData;
		}
		$doc.dispatchEvent(e);
	}-*/;
	
	
	
}