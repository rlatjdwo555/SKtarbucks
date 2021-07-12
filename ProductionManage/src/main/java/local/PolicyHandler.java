package local;

import local.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyHandler{

    @Autowired
    ProductionRepository productionRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderRequested_ProductionComplete(@Payload Requested requested){

        if(requested.isMe()){
            //  주문 요청으로 인한 제조 확정
            System.out.println("##### 주문 요청으로 인한 제조 확정: " + requested.toJson());
            if(requested.isMe()){
                Production temp = new Production();
                temp.setStatus("REQUEST_COMPLETED");
                temp.setCustNm(requested.getCustNm());
                temp.setCafeId(requested.getCafeId());
                temp.setCafeNm(requested.getCafeNm());
                temp.setOrderId(requested.getId());
                productionRepository.save(temp);
            }
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCafeDeleted_ForcedProductionCancel(@Payload CafeDeleted cafeDeleted){

        if(cafeDeleted.isMe()){
            System.out.println("##### listener ForcedProductionCanceled : " + cafeDeleted.toJson());
            //  카페 종료로 인해 제조 상태 변경
            List<Production> list = productionRepository.findByCafeId(String.valueOf(cafeDeleted.getId()));
            for(Production temp : list){
                if(!"CANCELED".equals(temp.getStatus())) {
                    temp.setStatus("FORCE_CANCELED");
                    productionRepository.save(temp);
                }
            }
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCanceled_ProductionCancel(@Payload Canceled canceled){

        if(canceled.isMe()){
            //  주문 취소로 인한 취소
            Production temp = productionRepository.findByOrderId(canceled.getId());
            temp.setStatus("CANCELED");
            productionRepository.save(temp);

        }
    }

}
