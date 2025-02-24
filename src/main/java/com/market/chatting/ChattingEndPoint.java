package com.market.chatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.json.JSONObject;

public class ChattingEndPoint {
	private static List<Object> clients = Collections.synchronizedList(new ArrayList<>());
	private String nickname;
	private ChattingService service = SpringContext.getApplicationContext().getBean(ChattingService.class);

	// 클라이언트가 웹소켓 통신에 대한 요청을 처음 보낸 때를 캐치하는 메서드(처음 접속한 순간 = 웹소켓 통신 스트림이 열린 순간)
	// 클라이언트의 웹소켓 첫 접속이 이뤄진 순간 자동으로 실행되는 메서드
	@OnOpen
	// 지금 접속한 클라이언트의 session 값을 매개변수로 받음.
	public void onOpen(Session session, EndpointConfig config) { 
		// modifyHandshake 과정에서 셋팅해줬던 닉네임 값 꺼내기 
		String nickname = (String)config.getUserProperties().get("nickname");
		System.out.println("접속한 사용자 : " + nickname);
		this.nickname = nickname; // 멤버필드로 셋팅해두기(onMessage메서드에서 사용가능하게끔)
		
		System.out.println("접속됨");
		clients.add(session); // 멤버필드list에 새로 접속한 클라이언트 세션 추가
		for(Object client : clients) {
			System.out.println(client);
		}
	}
	
	// 클라이언트가 웹소켓을 통해 서버와 연결되어있는 동안 send라는 함수를 사용했을때 실행되는 메서드
	@OnMessage
	public void onMesssage(Session session, String message) {// message 변수에 클라이언트가 전송한 메세지 담김
		System.out.println("메세지 발신자 : " + this.nickname);
		System.out.println("message : " + message);
		
		JSONObject obj = new JSONObject();
		obj.put("nickname", this.nickname);
		obj.put("message", message);
		System.out.println(obj.toString());
		
//		try {
//			service.insert(new ChattingDTO(this.nickname, message));
//		}catch(Exception e) {
//			e.printStackTrace();
//			return;
//		}
		
		
		
		try {
			// 여러명이 메세지를 주고받는 상황
			// (clients 리스트를 참조해 for문을 돌리는 도중에 접속이 끊기는 경우)
			// concurrentModificationException 발생할 수 있음(동시성 오류)
			
			// 만약 아래의 for문을 수행하는 도중 clients 를 참조하는 다른 작업이 생긴다면
			// 이 for문을 모두 수행할때까지 다른 작업은 대기하게끔 만드는 작업. 
			synchronized (clients) {
				for(Object client : clients) {
					if(session != client) { // 나에게는 메세지 다시 보내지 않음
						// 접속된 모든 클라이언트들에게 메세지 보내기
						// getBasicRemote() 엔드포인트+클라이언트 사이의 스트림 주소값을 알아낼 수 있는 메서드
						((Session) client).getBasicRemote().sendText(obj.toString());
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@OnClose // 연결이 끊어지면 이 어노테이션 붙은 메서드가 실행됨
	public void onClose(Session session) {
		// clients 에서 지금 연결을 끊은 클라이언트의 session 삭제
		clients.remove(session);
		System.out.println("클라이언트의 접속이 끊어졌습니다.");
	}
}
