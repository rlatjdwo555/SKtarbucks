package local;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Order_table")
public class Order {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long cafeId;
    private String chkDate;
    private String custNm;
    private String status;
    private String cafeNm;

    @PostPersist
    public void onPostPersist(){;

        // 주문 요청함 ( Req / Res : 동기 방식 호출)
        local.external.Cafe cafe = new local.external.Cafe();
        cafe.setCafeId(getCafeId());
        // mappings goes here
        OrderManageApplication.applicationContext.getBean(local.external.CafeService.class)
            .orderRequest(cafe.getCafeId(),cafe);


        Requested requested = new Requested();
        BeanUtils.copyProperties(this, requested);
        requested.publishAfterCommit();
    }


    @PostUpdate
    public void onPostUpdate(){

        System.out.println("#### onPostUpdate :" + this.toString());

        if("CANCELED".equals(this.getStatus())) {
            Canceled canceled = new Canceled();
            BeanUtils.copyProperties(this, canceled);
            canceled.publishAfterCommit();
        }
        else if("FORCE_CANCELED".equals(getStatus())){
            ForceCanceled forceCanceled = new ForceCanceled();
            BeanUtils.copyProperties(this, forceCanceled);
            forceCanceled.publishAfterCommit();
        }
        else if("REQUEST_COMPLETED".equals(getStatus())){
            System.out.println(getStatus());
            System.out.println("## REQ Info : " + this.getCafeId());
            System.out.println("## REQ Info : " + this.getCafeId());
            RequestCompleted requestCompleted = new RequestCompleted();
            BeanUtils.copyProperties(this, requestCompleted);
            requestCompleted.publishAfterCommit();
        }

    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCafeId() {
        return cafeId;
    }

    public void setCafeId(Long cafeId) {
        this.cafeId = cafeId;
    }
    public String getChkDate() {
        return chkDate;
    }

    public void setChkDate(String chkDate) {
        this.chkDate = chkDate;
    }
    public String getCustNm() {
        return custNm;
    }

    public void setCustNm(String custNm) {
        this.custNm = custNm;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getCafeNm() {
        return cafeNm;
    }

    public void setCafeNm(String cafeNm) {
        this.cafeNm = cafeNm;
    }




}
