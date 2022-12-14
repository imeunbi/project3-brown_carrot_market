package com.itwill.brown_carrot_market.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.itwill.brown_carrot_market.dto.ChatContents;
import com.itwill.brown_carrot_market.dto.ChatRoom;
import com.itwill.brown_carrot_market.dto.ChatRoomListView;
import com.itwill.brown_carrot_market.dto.Orders;
import com.itwill.brown_carrot_market.dto.Product;
import com.itwill.brown_carrot_market.dto.ProductImage;
import com.itwill.brown_carrot_market.dto.Promise;
import com.itwill.brown_carrot_market.dto.UserInfo;
import com.itwill.brown_carrot_market.service.ChatService;
import com.itwill.brown_carrot_market.service.OrdersService;
import com.itwill.brown_carrot_market.service.ProductService;
import com.itwill.brown_carrot_market.service.UserInfoService;


@RestController
@ServerEndpoint(value = "/replyEcho")
public class ReplyEchoHandler {
	@Autowired
	private ChatService chatService;
	
	@Autowired
	private OrdersService ordersService;
	
	@Autowired
	private UserInfoService userService;
	@Autowired private ProductService productService;
	private static Map<String, Session> userSessions = new HashMap();
	String userId="";
	String userKey="";
	
	private int item=0;

	//????????? ??????
	   @PostMapping(value = "/get_id")
	   public Map returnSessionCheck(HttpSession httpSession) {
		   Map resultMap=new HashMap();
		   String userImg=null;
	      String mId = (String)httpSession.getAttribute("sUserId");
	      this.userId=mId;
	      System.out.println("get_id ??????:"+mId);
	      try {
	    	  userImg=userService.findUser(mId).getUser_profile();
	      }catch (Exception e) {
	    	  e.printStackTrace();
	      }
	      
	      resultMap.put("mId", mId);
	      resultMap.put("userImg", userImg);
	      return resultMap;
	   }
	 
	
	 //?????? ?????? ????????????
	
	@PostMapping(value = "/chat_detail_rest")
	public Map chatDetail_rest(@RequestBody Map<String, String> chatList) throws Exception  {
		Map resultMap = new HashMap();
		int code = 1;
		String url = "";
		String msg = "";
		String yourId = "";
		String yourImg="";
		//int room_no = Integer.parseInt(c_room_no);
//		String room_no = c_room_no.substring(7);
		String room_no = chatList.get("c_room_no");
		String mId=chatList.get("loginId");
		String p_img;
		int checkSeller;
		
		ChatRoom chatRoom=chatService.chatRoomSelect(Integer.parseInt(room_no));
		
		if(chatRoom.getFrom_id().equals(mId)) {
			yourId=chatRoom.getTo_id();
			yourImg = userService.findUser(yourId).getUser_profile();
			checkSeller=0;
		}else {
			yourId=chatRoom.getFrom_id();
			yourImg = userService.findUser(yourId).getUser_profile();
			checkSeller=1;
		}
		double yourFreshness = userService.findUser(yourId).getUser_freshness();
		Product product = productService.selectByOne(chatRoom.getP_no());
		ArrayList<ProductImage> productImage = (ArrayList<ProductImage>)productService.selectProductImgList(chatRoom.getP_no());
		if(productImage.size()==0) {
			 p_img = "close.png";
		}else {
			 p_img = productImage.get(0).getPi_name();
			
		}
		List<ChatContents> resultList = new ArrayList<ChatContents>();
		try {
			chatService.chatReadUpdate(Integer.parseInt(room_no),yourId); //????????? ?????? ?????? ?????? ?????? ??????
			List<ChatContents> chatDetailList = chatService.chatSellectByRoom(Integer.parseInt(room_no));
			code = 1;
			msg = "??????";
			resultList = chatDetailList;
		} catch (Exception e) {
			code = 2;
			msg = "??????";
			e.printStackTrace();

		}
		System.out.println("?????? ??????"+mId);
		System.out.println("?????? ??????"+yourId);
		resultMap.put("code", code);
		resultMap.put("msg", msg);
		resultMap.put("yourId", yourId);
		resultMap.put("yourFreshness", yourFreshness);		
		resultMap.put("c_room_no", room_no);
		resultMap.put("yourImg", yourImg);
		resultMap.put("product", product);	
		resultMap.put("p_img", p_img);
		resultMap.put("data", resultList);
		resultMap.put("checkSeller", checkSeller);

		return resultMap;
	}
	
	// ??????-----------------------------------------------
		@PostMapping(value = "/chat_delete_rest", produces = "application/json;charset=UTF-8")
		public Map chatDelete_rest(@RequestBody Map<String, String> chatRoom) {
			Map resultMap = new HashMap();
			int code = 1;
			String url = "";
			String msg = "";
			String yourId = "";
			//int room_no = Integer.parseInt(c_room_no);
			String room_no = chatRoom.get("c_room_no");
			String loginId = chatRoom.get("loginId");

			List<ChatRoomListView> resultList = new ArrayList<ChatRoomListView>();
			chatService.chatRoomDelete(loginId,Integer.parseInt(room_no));
			
			
			try {
				List<ChatRoomListView> chatList = chatService.chatRoomSelectAll(loginId);
			
					for (ChatRoomListView chatRoomListView : chatList) {
							
							System.out.println(chatRoomListView.getYou_id());
							
							String img = userService.findUser(chatRoomListView.getYou_id()).getUser_profile();
							ArrayList<ProductImage> productImage = (ArrayList<ProductImage>)productService.selectProductImgList(chatRoomListView.getP_no());
							if(productImage.size()==0) {
								chatRoomListView.setP_img("pan.jpg");
							}else {
								chatRoomListView.setP_img(productImage.get(0).getPi_name());
								
							}
							
							System.out.println("?????????.."+img);
							chatRoomListView.setYou_image(img);
							chatRoomListView.setNot_read(chatService.chatNotRead(chatRoomListView.getC_room_no(),loginId));
					}
				code = 1;
				msg = "??????";
				resultList = chatList;
			} catch (Exception e) {
				code = 2;
				msg = "??????";
				e.printStackTrace();

			}
		

			resultMap.put("code", code);
			resultMap.put("msg", msg);
			resultMap.put("data", resultList);
			resultMap.put("c_room_no", room_no);

			return resultMap;
		}
	
		// ????????? reload-----------------------------------------------
				@PostMapping(value = "/chat_room_reload_rest", produces = "application/json;charset=UTF-8")
				public Map chatRoom_Reload_rest(@RequestBody Map<String, String> Id) {
					Map resultMap = new HashMap();
					int code = 1;
					String url = "";
					String msg = "";
					String yourId = "";
					//int room_no = Integer.parseInt(c_room_no);
					String loginId = Id.get("loginId");

					List<ChatRoomListView> resultList = new ArrayList<ChatRoomListView>();
					
					
					
					try {
						List<ChatRoomListView> chatList = chatService.chatRoomSelectAll(loginId);
					
							for (ChatRoomListView chatRoomListView : chatList) {
									
									System.out.println(chatRoomListView.getYou_id());
								
									String img = userService.findUser(chatRoomListView.getYou_id()).getUser_profile();
									ArrayList<ProductImage> productImage = (ArrayList<ProductImage>)productService.selectProductImgList(chatRoomListView.getP_no());
									if(productImage.size()==0) {
										chatRoomListView.setP_img("pan.jpg");
									}else {
										chatRoomListView.setP_img(productImage.get(0).getPi_name());
										
									}
									chatRoomListView.setYou_image(img);
									chatRoomListView.setNot_read(chatService.chatNotRead(chatRoomListView.getC_room_no(),loginId));
							}
						code = 1;
						msg = "??????";
						resultList = chatList;
					} catch (Exception e) {
						code = 2;
						msg = "??????";
						e.printStackTrace();

					}
				

					resultMap.put("code", code);
					resultMap.put("msg", msg);
					resultMap.put("data", resultList);
				

					return resultMap;
				}
			// ----------------------------------------------------------------------------
				//????????? ??????
				
				@PostMapping(value = "/chat_sell_rest")
				public Map chatSell_rest(@RequestBody Map<String, Product> reserve) throws Exception  {
					Map resultMap = new HashMap();

					Product product = reserve.get("product");
					product.setP_sell(1);
					productService.updateProductSell(1, product.getP_no());
					resultMap.put("product", product);	
					int o_no = ordersService.selectByP_No(product.getP_no()).getOrders_no();
					int result = ordersService.deleteOrders(o_no);					

					return resultMap;
				}
				//????????????
				
				@PostMapping(value = "/chat_reserve_rest")
				public Map chatReserve_rest(@RequestBody Map<String, String> reserve) throws Exception  {
					Map resultMap = new HashMap();
					
					int p_no = Integer.parseInt(reserve.get("p_no"));
					Product product = productService.selectByOne(p_no);
					
					
					product.setP_sell(2);
					productService.updateProductSell(2, product.getP_no());
					
					String you_Id = reserve.get("yourId");
					Orders orders=new Orders();
					orders.setUserinfo(new UserInfo(you_Id, null, null, null, null, 0, 0, null, null));
					orders.setProduct(product);
					int result = ordersService.insertOrders(orders);
			
					resultMap.put("product", product);	


					return resultMap;
				}
				
				@PostMapping(value = "/chatdetail_reserve_rest")
				public Map chatDetailReserve_rest(@RequestBody Map<String, String> reserve) throws Exception  {
					Map resultMap = new HashMap();
					
					int p_no = Integer.parseInt(reserve.get("p_no"));
					Product product = productService.selectByOne(p_no);

					
					product.setP_sell(2);
					productService.updateProductSell(2, product.getP_no());
					
					String you_Id = reserve.get("yourId");
					Orders orders=new Orders();
					orders.setUserinfo(new UserInfo(you_Id, null, null, null, null, 0, 0, null, null));
					orders.setProduct(product);
					int result = ordersService.insertOrders(orders);
					
					resultMap.put("product", product);	


					return resultMap;
				}
				 //????????????
				
				@PostMapping(value = "/chat_soldout_rest")
				public Map chatSoldOut_rest(@RequestBody Map<String, String> reserve) throws Exception  {
					Map resultMap = new HashMap();

					int p_no = Integer.parseInt(reserve.get("p_no"));
					Product product = productService.selectByOne(p_no);
					
					if(ordersService.selectByOrdersCount(p_no)>0) {
						
						product.setP_sell(3);
						productService.updateProductSell(3, product.getP_no());
						
					}else {
						product.setP_sell(3);
						productService.updateProductSell(3, product.getP_no());
						
					String you_Id = reserve.get("yourId");
					Orders orders=new Orders();
					orders.setUserinfo(new UserInfo(you_Id, null, null, null, null, 0, 0, null, null));
					orders.setProduct(product);
					int result = ordersService.insertOrders(orders);
					
					}
					resultMap.put("product", product);	


					return resultMap;
				}
				
				@PostMapping(value = "/chatdetail_soldout_rest")
				public Map chatDetailSoldout_rest(@RequestBody Map<String, String> reserve) throws Exception  {
					Map resultMap = new HashMap();
					
					int p_no = Integer.parseInt(reserve.get("p_no"));
					Product product = productService.selectByOne(p_no);
					if(ordersService.selectByOrdersCount(p_no)>0) {
						
						product.setP_sell(3);
						productService.updateProductSell(3, product.getP_no());
						
					}else {

					
					product.setP_sell(3);
					productService.updateProductSell(3, product.getP_no());
					
					String you_Id = reserve.get("yourId");
					Orders orders=new Orders();
					orders.setUserinfo(new UserInfo(you_Id, null, null, null, null, 0, 0, null, null));
					orders.setProduct(product);
					int result = ordersService.insertOrders(orders);
					}
					resultMap.put("product", product);	


					return resultMap;
				}

	
	/******************* ?????? ??????****************************************************/

	@OnOpen
	public void handleOpen(Session session) {
		String mId="";
		String c_room_no="";
		String qStr=(String)session.getQueryString();
		String key="";
		if(qStr.contains("&")) {
			String[] qStrArray=qStr.split("&");
			mId=qStrArray[0].trim();
			c_room_no=qStrArray[1].trim();
			key=mId+"&"+c_room_no;
		}else {
			mId=(String)session.getQueryString();
			key=mId;
		}
		this.userKey=key;
		//String mId = (String)session.getQueryString();
		System.out.println("?????? ??????:" + session);
		System.out.println("?????????:" + mId);
		userSessions.put(key, session);
		System.out.println(userSessions);
	}

	/*
	 * public void afterConnectionEstablished(WebSocketSession session) throws
	 * Exception{ System.out.println("?????? ??????:"+session); String
	 * mId=(String)session.getAttributes().get("user_id"); //session??? ????????? id ??????
	 * userSessions.put(mId, session); }
	 */

	@OnMessage
	public void handleMessage(String message, Session session) {
		System.out.println("---------ON MESSAGE--------");
		System.out.println(message);

		System.out.println("????????? ????????? ??????:" + session);
		
		JSONObject jsonObj = new JSONObject(message);
		String code=jsonObj.getString("code");
		/**********code ??????*********/
		String yourId = jsonObj.getString("your_id");
		String myId=jsonObj.getString("mId");
		System.out.println("?????????(JSONObject) ????????? ??? ?????????(key):" + myId);
		System.out.println("?????????(JSONObject) ?????? ?????? ?????????(key):" + yourId);

		

		JSONArray jsonArr = (JSONArray) jsonObj.get("data");
		JSONObject jsonChat = (JSONObject) jsonArr.get(0);
		Session myChatSession=userSessions.get(myId+"&"+jsonChat.getString("c_room_no"));
		
		
		
		System.out.println("????????? ????????? ??????:" + myChatSession);

		System.out.println("jsonData.data ?????? ??????:" + jsonChat.getString("c_content"));

		ChatContents newChat = new ChatContents(0, jsonChat.getString("c_content"), "time", 0,
				jsonChat.getString("user_id"), Integer.parseInt(jsonChat.getString("c_room_no")));
		System.out.println("?????? DB ?????? ??? ??????:" + newChat);
		
		
		
		if(code.equals("1")) {
			jsonChat.put("code", 1);

		try {
			Session yourSession = userSessions.get(yourId+"&"+jsonChat.getString("c_room_no"));
			System.out.println("????????? ?????? ??????:" + yourSession);

			System.out.println("?????? ????????? ????????? ????????? ?????? ??????");
			if (yourSession != null&&myChatSession!=null) {
				jsonChat.put("toastId", "youExist");
				jsonChat.put("c_read", 1);
				yourSession.getBasicRemote().sendText(jsonChat.toString());
				myChatSession.getBasicRemote().sendText(jsonChat.toString());
			}else if(yourSession!=null) {
				myChatSession.getBasicRemote().sendText(jsonChat.toString());
			}else {
				for(String key : userSessions.keySet()) {
					jsonChat.put("toastId", yourId);
					Session s=userSessions.get(key);
					
					System.out.println("?????? ?????? ?????? ?????? :"+s);
					s.getBasicRemote().sendText(jsonChat.toString());
				}
			}
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
		else if(code.equals("2")) {
			jsonChat.put("code", 2);
			try {
			Session yourSession = userSessions.get(yourId+"&"+jsonChat.getString("c_room_no"));
			System.out.println("????????? ?????? ??????:" + yourSession);

			System.out.println(">>>?????? ????????? ????????? ?????? ??????");
			if (yourSession != null&&myChatSession!=null) {
				System.out.println(">>>?????? ????????? ???????????? ????????? ?????? ??????");
				jsonChat.put("c_read", 1);
				yourSession.getBasicRemote().sendText(jsonChat.toString());
				myChatSession.getBasicRemote().sendText(jsonChat.toString());
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}else if(code.equals("3")) {
			jsonChat.put("code", 3);
			
			try {
				Session yourSession = userSessions.get(yourId+"&"+jsonChat.getString("c_room_no"));

				System.out.println("?????? ??????");
				if (yourSession != null&&myChatSession!=null) {
					jsonChat.put("c_read", 1);
					yourSession.getBasicRemote().sendText(jsonChat.toString());
					myChatSession.getBasicRemote().sendText(jsonChat.toString());
				}else if(yourSession!=null) {
					myChatSession.getBasicRemote().sendText(jsonChat.toString());
				}else {
					for(String key : userSessions.keySet()) {
						jsonChat.put("toastId", yourId);
						Session s=userSessions.get(key);
						
						System.out.println("?????? ?????? ?????? ?????? :"+s);
						s.getBasicRemote().sendText(jsonChat.toString());
					}
				}
				
				}catch (Exception e) {
					e.printStackTrace();
				}
		}
		
		
	}

	
	//*********************????????? DB ?????? 
	
	@PostMapping(value = "/chat_message_rest")
	public String insertChat(@RequestBody Map<String, String> messages) {
	
		
		int rowCount = 0;
		System.out.println(String.valueOf(messages.get("user_id")));
		ChatContents newChat = new ChatContents(0, String.valueOf(messages.get("c_content")), null, null,
				String.valueOf(messages.get("user_id")), Integer.valueOf(messages.get("c_room_no")));

		rowCount = chatService.insertChat(newChat);
		String sendTime=chatService.chatSellectOne(newChat.getC_content_no()).getSend_time();
		int c_content_no=newChat.getC_content_no();
		// insert ??? ????????????????????? jsonObject ???????????? ?????????

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject();
			//jsonObject.put("rowCount", rowCount);
			jsonObject.put("send_time", sendTime);
			jsonObject.put("c_content_no",c_content_no);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject.toString();

	}

	@OnClose
	public void handleClose(Session session) {
		System.out.println("socket ??????:" + userKey);
		userSessions.remove(userKey);
		System.out.println(userSessions);
		
	}
	
	
	
	/***** ?????? ?????? ***/
	@PostMapping(value="/promise_insert_rest")
	public Map insertPromise(@RequestBody Map<String,String> message) {
		Map resultMap = new HashMap();
		String msg="";
		Promise newPromise=new Promise(Integer.valueOf(message.get("c_room_no")), Double.valueOf(message.get("c_app_lat")), Double.valueOf(message.get("c_app_lng")), String.valueOf(message.get("c_app_date")),
				String.valueOf(message.get("c_appspot")));
		System.out.println("????????? ??????:"+newPromise);
		
		ChatContents newChat = new ChatContents(0, String.valueOf(message.get("c_content")), null, null,
				String.valueOf(message.get("user_id")), Integer.valueOf(message.get("c_room_no")));

	
		try {
		System.out.println("?????? ??????Contents ??????:"+newChat);
		chatService.promiseInsert(newPromise);
		chatService.insertChat(newChat);
		msg="?????? ?????? ??????";
		}catch (Exception e) {
			e.printStackTrace();
		}
		resultMap.put("msg", msg);
		return resultMap;
	}
	
	//?????? ?????? 
	@PostMapping(value="/promise_delete")
	public Map deletePromise(@RequestBody Map<String,String> message) {
		Map resultMap=new HashMap();
		String code="";
		
		ChatContents newChat = new ChatContents(0, String.valueOf(message.get("c_content")), null, null,
				String.valueOf(message.get("user_id")), Integer.valueOf(message.get("c_room_no")));
		try {
			chatService.promiseDelete(Integer.valueOf(message.get("c_room_no")));
			chatService.insertChat(newChat);
			code="2";
		}catch (Exception e) {
			e.printStackTrace();
		}
		resultMap.put("code", code);
		return resultMap;
	}
	
	//?????? ??????????????? ?????? 
	@PostMapping(value="/promise_check",produces = "application/json;charset=UTF-8")
	public Map promiseExist(@RequestParam int c_room_no) {
		Map resultMap = new HashMap();
		String code="";
		Promise promise=null;
		try {
			promise=chatService.promiseSelect(c_room_no);
			if(promise!=null) {
				code="1"; //?????? ?????? ??????
			}else {
				code="2"; //?????????????????? 
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		resultMap.put("data",promise);
		resultMap.put("code", code);
		return resultMap;
	}
	
	@PostMapping(value="/promise_update")
	public Map updatePromise(@RequestBody Map<String,String> message){
		Map resultMap = new HashMap();
		String msg="";
		
		Promise updatePromise=new Promise(Integer.valueOf(message.get("c_room_no")), Double.valueOf(message.get("c_app_lat")), Double.valueOf(message.get("c_app_lng")), String.valueOf(message.get("c_app_date")),
				String.valueOf(message.get("c_appspot")));
		
		ChatContents newChat = new ChatContents(0, String.valueOf(message.get("c_content")), null, null,
				String.valueOf(message.get("user_id")), Integer.valueOf(message.get("c_room_no")));
		
		try {
			chatService.promiseUpdate(updatePromise);
			chatService.insertChat(newChat);
			msg="???????????? ??????";
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		resultMap.put("msg", msg);
		return resultMap;
		
	}
	
	@PostMapping(value="/promise_alert")
	public Map promiseAlert(@RequestParam String user_id) {
		Map resultMap = new HashMap();
		String code="";
		List<Promise> result=null;
		
		try {
			result=chatService.promiseAlert(user_id);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		resultMap.put("data", result);
		return resultMap;
	}
	
	//???????????????????????? 
	
	//??????????????????
	@PostMapping(value="/readChat_num")
	public Map readChatnum(@RequestParam String user_id) {
		Map resultMap = new HashMap();
		String code="";
		int result=0;
		
		try {
			result=chatService.selectReadChatCount(user_id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		resultMap.put("data", result);
		return resultMap;
	}
	

}
