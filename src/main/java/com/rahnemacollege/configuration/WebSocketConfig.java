package com.rahnemacollege.configuration;

import com.rahnemacollege.model.User;
import com.rahnemacollege.service.BidService;
import com.rahnemacollege.service.UserService;
import com.rahnemacollege.util.JwtTokenUtil;
import com.rahnemacollege.util.exceptions.MessageException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.ArrayList;
import java.util.Objects;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private JwtTokenUtil tokenUtil;
    private UserService userService;
    private BidService bidService;

    public WebSocketConfig(JwtTokenUtil tokenUtil, UserService userService, BidService bidService) {
        this.tokenUtil = tokenUtil;
        this.userService = userService;
        this.bidService = bidService;
    }

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketConfig.class);


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/auction", "/app") //socket_subscriber
                .enableSimpleBroker("/app", "/auction"); //socket_publisher
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/socket")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (headerAccessor != null && StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
                    logger.info("try to connect");
                    String jwtToken = Objects.requireNonNull(headerAccessor.getFirstNativeHeader("auth")).substring(7);
                    String deviceId = Objects.requireNonNull(headerAccessor.getFirstNativeHeader("deviceID"));
                    if(deviceId == null)
                        throw new MessageException(com.rahnemacollege.util.exceptions.Message.USER_NOT_FOUND);
                    String id = tokenUtil.getIdFromToken(jwtToken);
                    if (tokenUtil.isTokenExpired(jwtToken))
                        throw new MessageException(com.rahnemacollege.util.exceptions.Message.TOKEN_NOT_FOUND);
                    User user = userService.findUserId(Integer.valueOf(id));
                    bidService.addDeviceId(deviceId, user);
                    Authentication u = new UsernamePasswordAuthenticationToken(deviceId, user.getPassword(), new ArrayList<>());
                    headerAccessor.setUser(u);
                    logger.info("the user with id " + user.getId() + " connected with deviceId " + deviceId);
                }
                if (headerAccessor != null && StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
                    System.err.println(headerAccessor.getDestination());
                }if (headerAccessor != null && StompCommand.DISCONNECT.equals(headerAccessor.getCommand()) && headerAccessor.getUser() != null) {
                    Integer userId = bidService.getUserId(headerAccessor.getUser().getName());
                    if(userId == null)
                        return message;
                    User user = userService.findUserId(userId);
                    bidService.removeFromAllAuction(user);
                    bidService.removeDeviceId(headerAccessor.getUser().getName());
                }
                return message;
            }
        });
    }

}

