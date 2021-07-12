package local;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="MyPage_table")
public class MyPage {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long orderId;
        private String custNm;
        private String cafeNm;
        private String status;


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }
        public String getCustNm() {
            return custNm;
        }

        public void setCustNm(String custNm) {
            this.custNm = custNm;
        }
        public String getCafeNm() {
            return cafeNm;
        }

        public void setCafeNm(String cafeNm) {
            this.cafeNm = cafeNm;
        }
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

}
