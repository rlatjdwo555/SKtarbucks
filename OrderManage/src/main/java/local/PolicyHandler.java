package local;

import local.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PolicyHandler{

    @Autowired
    OrderRepository orderRepository;
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCafeDeleted_ForceCancel(@Payload CafeDeleted cafeDeleted){

        if(cafeDeleted.isMe()){
            System.out.println("##### listener ForceCancel : " + cafeDeleted.toJson());
            List<Order> list = orderRepository.findByCafeId(cafeDeleted.getId());
            for(Order temp : list){
                // 본인이 취소한건은 제외
                if(!"CANCELED".equals(temp.getStatus())) {
                    temp.setStatus("FORCE_CANCELED");
                    orderRepository.save(temp);
                }
            }
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverProductionCompleted_RequestComplete(@Payload ProductionCompleted productionCompleted){

        if(productionCompleted.isMe()){
            System.out.println("##### listener RequestComplete : " + productionCompleted.toJson());

            Optional<Order> temp = orderRepository.findById(productionCompleted.getOrderId());
            Order target = temp.get();
            System.out.println("##### RequestComplete : " + target.toString());
            target.setCafeId(Long.parseLong(productionCompleted.getCafeId()));
            target.setStatus(productionCompleted.getStatus());
            orderRepository.save(target);
        }
    }

}
