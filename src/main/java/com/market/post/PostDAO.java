package com.market.post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PostDAO {
	
	@Autowired
	private SqlSession session;
	
	
	// 게시글 저장
	public void insert(PostDTO dto) throws Exception{ 
		session.insert("postMapper.insert", dto);
	}
	
	// 게시글 수정
	public void modify(PostDTO dto) throws Exception{ 
		session.update("postMapper.modify", dto);
	}

	//게시글 삭제
	public void postDelete(int post_seq) throws Exception{
		session.delete("postMapper.postDelete", post_seq);
	}
	//state 업데이트
	public void toPost_state(PostDTO dto) throws Exception{
		session.delete("postMapper.toPost_state", dto);
	}
	//채팅수 업데이트
	public void update_chatting_cnt(int post_seq, int post_chatting_cnt) throws Exception{
		Map<String,Object> map= new HashMap<>();
		map.put("post_seq", post_seq);
		map.put("post_chatting_cnt", post_chatting_cnt);
		session.delete("postMapper.update_chatting_cnt", map);
	}
	// 새로운 게시글 시퀀스 번호 생성
	public int selectSeq() throws Exception{ 
		return session.selectOne("postMapper.selectSeq");
	}
	// post_seq 번호로 조회
	public PostDTO selectPost_seq(int post_seq) throws Exception{ 
		return session.selectOne("postMapper.selectPost_seq", post_seq);
	}
	//페이지 계산 (메인페이지)
	public int getPageNavi(String post_addr, String search) throws Exception{
		Map<String,Object> map= new HashMap<>();
		map.put("post_addr", post_addr);
		map.put("search", search);
		return session.selectOne("postMapper.getPageNavi",map);
	}
	//페이지 계산 (검색)
	public int getPageNavi_s(String post_addr, String search) throws Exception{
		Map<String,Object> map= new HashMap<>();
		map.put("post_addr", post_addr);
		map.put("search", search);
		return session.selectOne("postMapper.getPageNavi_s",map);
	}
	// 조인된 테이블 데이터 조회
	public List<Map<String,Object>> selectJoin(int start,int end) throws Exception{ 
		Map<String,Object> map= new HashMap<>();
		map.put("start", start);
		map.put("end", end);
		return session.selectList("postMapper.selectJoin",map);
	}
	// 데이터검색
	public List<Map<String,Object>> search(int start,int end, String post_addr,String search) throws Exception{ 
		Map<String,Object> map= new HashMap<>();
		map.put("start", start);
		map.put("end", end);
		map.put("post_addr", post_addr);
		map.put("search", search);
		return session.selectList("postMapper.search",map);
	}
	// 조회수 업
	public void inquiry_cnt(int post_seq) throws Exception{ 
		session.update("postMapper.inquiry_cnt", post_seq);
	}
	// 관심수 업데이트
	public void interestUpdate(int post_interest_cnt, int post_seq) throws Exception{ 
		Map<String, Integer> map = new HashMap<>();
		map.put("post_interest_cnt", post_interest_cnt);
		map.put("post_seq", post_seq);
		session.update("postMapper.interestUpdate", map);
	}

	// 관심수 업데이트
	public Map<String, Object> selectPost_member(int post_seq) throws Exception{ 
		return session.selectOne("postMapper.selectPost_member", post_seq);
	}
	
}
