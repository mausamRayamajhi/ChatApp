package com.mausam;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Hello world!
 *
 */
public class ChatApplication implements MessageListener
{
    private JmsTemplate chatJMSTemplate;
    private Topic chatTopic;
    private static String userId;

    public static void main( String[] args ) throws JMSException, IOException {
 
        if(args.length !=1){
            System.out.println("USER NAME IS REQUIRED....!!");
        }else{
            userId = args[0];
            ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
            ChatApplication chatApplication = (ChatApplication)ctx.getBean("chatApplication");
            TopicConnectionFactory factory = (TopicConnectionFactory)chatApplication.chatJMSTemplate.getConnectionFactory();
            TopicConnection topicConnection = factory.createTopicConnection();
            chatApplication.publish(topicConnection,chatApplication.chatTopic,userId);
            chatApplication.subscribe(topicConnection,chatApplication.chatTopic,chatApplication);

        }

    }

    private void subscribe(TopicConnection topicConnection, Topic chatTopic, ChatApplication chatApplication) throws JMSException {
        TopicSession topicSession = topicConnection.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);
        TopicSubscriber subscriber = topicSession.createSubscriber(chatTopic);
        subscriber.setMessageListener(chatApplication);
    }

    private void publish(TopicConnection topicConnection, Topic chatTopic, String userId) throws JMSException, IOException {
        TopicSession topicSession = topicConnection.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);
        TopicPublisher publisher = topicSession.createPublisher(chatTopic);
        topicConnection.start();

        BufferedReader reader = new BufferedReader( new InputStreamReader(System.in));
        while (true){
            String msgToSend = reader.readLine();
            if (msgToSend.equalsIgnoreCase("exit")){
                topicConnection.close();
                System.exit(0);
            }else{
                TextMessage msg = (TextMessage)topicSession.createTextMessage();
                msg.setText("\n["+userId+" : " +msgToSend +"]");
                publisher.publish(msg);
            }
        }
    }

    @Override
    public void onMessage(Message message) {

        if (message instanceof TextMessage){
            try {
                String msgText = ((TextMessage)message).getText();
                if (!msgText.startsWith("["+userId)) System.out.println(msgText);
            } catch (JMSException e) {
               String errMsg = "An error occured extracting message";
            }
        }else {
            String errMsg = "Message is not of expected TextMessage";
            System.out.println(errMsg);
        }
    }

    public JmsTemplate getChatJMSTemplate() {
        return chatJMSTemplate;
    }

    public void setChatJMSTemplate(JmsTemplate chatJMSTemplate) {
        this.chatJMSTemplate = chatJMSTemplate;
    }

	public Topic getChatTopic() {
		return chatTopic;
	}

	public void setChatTopic(Topic chatTopic) {
		this.chatTopic = chatTopic;
	}

	public static String getUserId() {
		return userId;
	}

	public static void setUserId(String userId) {
		ChatApplication.userId = userId;
	}
}
