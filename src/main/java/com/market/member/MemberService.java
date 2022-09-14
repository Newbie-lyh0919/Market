package com.market.member;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.market.blackList.BlackListDAO;
import com.market.blackList.BlackListDTO;

	/*
	dao를 통해 데이터를 추가,수정,삭제,조회해야하는 경우 메서드 생성
	혹은 요청/응답과는 별개로 추가적으로 가공해야하는 데이터가 있는 경우 
		
		컨트롤러-> 클라이언트로부터 직접으로 요청을 받고 그 요청에 따라
	 * 추가적인 데이터 가공이 필요하거나, 혹은 DB를 통해 데이터
	 * 수정/삭제/조회/추가해야 하는 경우 controller가 직접 dao 를 호출하지 않고
	 * service 에게 그 작업을 전달함 
	 * -> 그럼 service의 호출된 메서드가 추가적으로 처리해야하는 가공이나 dao 호출을 통해
	 * 비니지스 로직을 수행하고 그에 따른 결과값만 바로 controller에게 전달해 줌.
	 * -> controller는 결과값을 받아서 판단에 따라 응답값을 어떻게 클라이언트에 되돌려
	 * 줄지 결정한다. 
	 * 
	 * 즉 controller는 클라이언트의 요청과 응답관 관련된 직접적인 일들만 처리
	 * 나머지 뒤에서 보이지 않는 일들은 서비스가 처리. 
	 * */
	@Service
	public class MemberService {
	@Autowired
	private MemberDAO memberDAO;
	@Autowired
	private BlackListDAO blackListDAO;
	@Autowired
	private SqlSession session;
	@Inject
	BCryptPasswordEncoder pwdEncoder;
  

	
  //용현
	// 로그인
	public MemberDTO login(String user_id) throws Exception{
		return memberDAO.login(user_id);
	}
	
	// 블랙리스트 해당 인원 확인
	public int Checkbk(String user_id) throws Exception{
		return blackListDAO.checkBlack(user_id);
	}
	
//	// 카카오톡 로그아웃
//	public void kakaoLogout(String user_k) {
//	    String reqURL = "https://kapi.kakao.com/v1/user/logout";
//	    try {
//	        URL url = new URL(reqURL);
//	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//	        conn.setRequestMethod("POST");
//	        conn.setRequestProperty("Authorization", "Bearer " + user_k);
//	        
//	        int responseCode = conn.getResponseCode();
//	        System.out.println("responseCode : " + responseCode);
//	        
//	        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//	        
//	        String result = "";
//	        String line = "";
//	        
//	        while ((line = br.readLine()) != null) {
//	            result += line;
//	        }
//	        System.out.println(result);
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//	}
	
	// 아이디 중복확인
	public boolean checkLogin(String user_id) throws Exception{
		return memberDAO.checkLogin(user_id);
	}
	
	// 아이디 찾기
	public String findId(String user_name, String user_phone) throws Exception{
		return memberDAO.findId(user_name, user_phone);
	}
	
	// 이메일 전송
	public String sendEmail(String user_id) {
		Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.naver.com");
        props.put("mail.smtp.port", 465);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.naver.com");
        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("leeyoung0919", "dydrkfl1234!");
            }
        });
        session.setDebug(true);
        System.out.println("user_id " + user_id);
        try {
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress("leeyoung0919@naver.com", "싸다구 장터"));		// 별명 설정
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(user_id));
            mimeMessage.setSubject("["+"싸다구 장터"+"]"+ "임시 비밀번호 안내");
            
            String changed_pw = update_pw(user_id);
            
            mimeMessage.setContent("안녕하세요<br><strong>" + user_id + "</strong>님,<br>귀하께서 요청하신 임시 비밀번호 수신을 위해 발송된 메일입니다.<br><br>고객님의 임시 비밀번호는 <strong><font color=\"blue\">" + changed_pw + "</font></strong> 입니다.<br><br>로그인 후에는 새로운 비밀번호로 변경하셔야 합니다.<br>감사합니다.", "text/html;charset=euc-kr");
            Transport.send(mimeMessage);
            return "success";
        } catch (Exception e){
        	e.printStackTrace();
            return "fail";
        }
        
	}
	
	//비밀번호찾기
	public String update_pw(String user_id) throws Exception {
		
		// 임시 비밀번호 생성
		String user_pw = "";
		
		for (int i = 0; i < 12; i++) {
			user_pw += (char) ((Math.random() * 26) + 97);
		}
		// 비밀번호 암호화
		String changed_pw = pwdEncoder.encode(user_pw);
		
		// 비밀번호 변경
		memberDAO.update_pw(user_id, changed_pw);

		return user_pw;
	}
	
	// 회원가입
	public int signup(MemberDTO dto) throws Exception{
		return memberDAO.insert(dto);
	}
	
	// 카카오 회원가입
	public int kakaosignup(MemberDTO dto) throws Exception{
		return memberDAO.kakaoinsert(dto);
	}
	
	// 회원탈퇴
	public int memberdelete(String user_id) throws Exception{
		return memberDAO.memberdelete(user_id);
	}
	
	// 회원탈퇴시 비밀번호 확인
	public MemberDTO checkPw(String user_id) throws Exception{
		return memberDAO.checkPw(user_id);
	}
	
	// 닉네임 중복확인
	public boolean checkNickname(String user_nickname) throws Exception{
		return memberDAO.checkNickname(user_nickname);
	}
	
	// 휴대폰번호 중복확인
	public boolean checkPhone(String user_phone) throws Exception{
		return memberDAO.checkPhone(user_phone);
	}
	
  // 카카오 관련
	// 카카오톡 토큰 받기
	public String getAccessToken (String authorize_code) {
        String access_Token = "";
        String refresh_Token = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";

        try {
            URL url = new URL(reqURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //    POST 요청을 위해 기본값이 false인 setDoOutput을 true로

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            //    POST 요청에 필요로 요구하는 파라미터 스트림을 통해 전송
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
            StringBuilder sb = new StringBuilder();
            sb.append("grant_type=authorization_code");
            sb.append("&client_id=78df7cab2093de0eb394ceeec542eb6e");  //본인이 발급받은 key
            sb.append("&redirect_uri=http://192.168.20.19:8099/member/kakaoLogin");     // 본인이 설정해 놓은 경로
            sb.append("&code=" + authorize_code);
            bw.write(sb.toString());
            bw.flush();

            //    결과 코드가 200이라면 성공
            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            //    요청을 통해 얻은 JSON타입의 Response 메세지 읽어오기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            //    Gson 라이브러리에 포함된 클래스로 JSON파싱 객체 생성
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            access_Token = element.getAsJsonObject().get("access_token").getAsString();
            refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();

            System.out.println("access_token : " + access_Token);
            System.out.println("refresh_token : " + refresh_Token);

            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return access_Token;
    }
	
    public HashMap<String, Object> getUserInfo (String access_Token) {
        //    요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
        HashMap<String, Object> userInfo = new HashMap<>();
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        try {
            URL url = new URL(reqURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");

            //    요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty("Authorization", "Bearer " + access_Token);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);

            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

            String nickname = properties.getAsJsonObject().get("nickname").getAsString();
            String email = kakao_account.getAsJsonObject().get("email").getAsString();

            userInfo.put("nickname", nickname);
            userInfo.put("email", email);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return userInfo;
    }
    
    public void kakaologout(String access_Token) {
	    String reqURL = "https://kapi.kakao.com/v1/user/logout";
	    try {
	        URL url = new URL(reqURL);
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Authorization", "Bearer " + access_Token);
	        
	        int responseCode = conn.getResponseCode();
	        System.out.println("responseCode : " + responseCode);
	        
	        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        
	        String result = "";
	        String line = "";
	        
	        while ((line = br.readLine()) != null) {
	            result += line;
	        }
	        System.out.println(result);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
  //하륜
	public List<MemberDTO> selectAll(int start,int end) throws Exception{
		return memberDAO.selectAll(start,end);
	}
		public void mn_delete(String id) throws Exception{ 	//manager에서 체크박스 선택 삭제
			memberDAO.mn_delete(id);
	}	
	public List<MemberDTO> mSearch(String user_id) throws Exception{
		return memberDAO.mSearch(user_id);		
	}
	public HashMap<String,Object> getPageNavi(int curPage) throws Exception{
		int totalCnt = memberDAO.getPageNavi(); //전체 게시글의 개수
		int recordCntPerPage=10; //한페이지에 몇개의 데이터(게시글)을 띄워줄지
		int naviCntPerPage=5; //네비바에 몇개 단위로 페이징을 구성할지
		int pageTotalCnt =0;// 총 몇 페이지가 나올지			
		if(totalCnt % recordCntPerPage >0) {
			pageTotalCnt =totalCnt/recordCntPerPage +1;
		}else {
			pageTotalCnt =totalCnt/ recordCntPerPage;
		}				
		if(curPage<1) {
			curPage=1;		
		}else if(curPage>pageTotalCnt) {

		}						
		int startNavi =((curPage-1)/naviCntPerPage)*naviCntPerPage+1;
		int endNavi =startNavi + naviCntPerPage -1;
	
		if(pageTotalCnt < endNavi) {
			endNavi =pageTotalCnt; 							
		}		
		boolean needPrev = true;	
		boolean needNext = true;
		if(startNavi==1) {
			needPrev =false;			
		}
		if(endNavi ==pageTotalCnt) {
			needNext =false;
		}
		HashMap<String,Object> map =new HashMap<>();
		map.put("startNavi",startNavi);
		map.put("endNavi", endNavi);
		map.put("needPrev", needPrev);
		map.put("needNext", needNext);		
		return map;
				
	}
	
	//준철 주소 합치기
	public String makeAddr(String user_id)throws Exception{
		return memberDAO.makeAddr(user_id);	
	}

	public String e_makeAddr(String user_nickname)throws Exception{
		return memberDAO.e_makeAddr(user_nickname);	
	}
	public MemberDTO selectByNickname(String user_nickname)throws Exception{
		return memberDAO.selectByNickname(user_nickname);	
	}
}