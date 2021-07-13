# 4조 - 슼타벅스

# Table of contents

- [서비스 시나리오](#서비스-시나리오)
- [분석/설계](#분석설계)
  - [Event Storming](#Event-Storming)
  - [헥사고날 아키텍처 다이어그램 도출](#헥사고날-아키텍처-다이어그램-도출)
- [구현](#구현)
  - [시나리오 테스트결과](#시나리오-테스트결과)
  - [DDD의 적용](#DDD의-적용)
  - [Gateway 적용](#Gateway-적용)
  - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
  - [동기식 호출과 Fallback 처리](#동기식-호출과-Fallback-처리)
  - [비동기식 호출과 Eventual Consistency](#비동기식-호출-/-시간적-디커플링-/-장애격리-/-최종-(Eventual)-일관성-테스트)
 - [운영](#운영)
   - [CI/CD 설정](#CI/CD-설정)
   - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식호출-/-서킷브레이킹-/-장애격리)
   - [오토스케일 아웃](#오토스케일-아웃)
   - [무정지 재배포](#무정지-재배포)
   - [ConfigMap 사용](#ConfigMap-사용)

# 서비스 시나리오

## 기능적 요구사항
1. 카페주인이 카페 메뉴(음료이름, 주문가능일, 주문가능수)를 등록한다.
1. 고객이 주문을 요청한다. 
1. 고객의 주문 요청에 따라서 해당 음료의 주문가능 수가 감소한다. (Sync) 
1. 고객의 주문 상태가 주문 완료로 변경된다. (Async) 
1. 고객의 주문 완료에 따라서 주문관리의 해당 내역의 상태가 등록된다.
1. 고객이 주문을 취소한다.
1. 고객의 주문 취소에 따라서 해당 음료의 주문가능 수가 증가한다. (Async)
1. 고객의 주문 취소에 따라서 주문관리의 해당 내역의 상태가 주문 취소로 변경된다.
1. 카페주인이 메뉴 정보를 삭제한다.
1. 카페주인의 주문 정보 삭제에 따라서 주문관리의 해당 내역 상태가 강제취소로 변경된다.
1. 고객이 주문 진행내역 상태를 조회한다.

## 비기능적 요구사항
1. 트랜잭션
    1. 고객의 주문에 따라서 해당 날짜 / 음료의 주문가능 수가 감소한다. > Sync
    1. 고객의 취소에 따라서 해당 날짜 / 음료의 주문가능 수가 증가한다. > Async
1. 장애격리
    1. 제조 관리 서비스에 장애가 발생하더라도 주문 예약은 정상적으로 처리 가능하다.  > Async (event-driven)
    1. 서킷 브레이킹 프레임워크 > istio-injection + DestinationRule
1. 성능
    1. 고객은 본인의 주문 상태 및 이력 정보를 확인할 수 있다. > CQRS

# 분석/설계

## AS-IS 조직 (Horizontally-Aligned)
  ![1](https://user-images.githubusercontent.com/67453893/91927140-baa8af00-ed13-11ea-8ead-0d4616a6da56.png)

## TO-BE 조직 (Vertically-Aligned)
  ![2](https://i.imgur.com/Fl8cBoy.png)


# Event Storming 

### 이벤트 도출
![image](https://i.imgur.com/ze0TtbL.png)


### 부적격 이벤트 탈락
![image2](https://i.imgur.com/7KEHqkZ.png)

    - 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함

## Event Storming 결과
수정한 이벤트스토밍
![eventstorming](https://i.imgur.com/RH6rs40.png)




### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://i.imgur.com/4YEKhF3.png)
![image](https://i.imgur.com/MhV6hsF.png)
![image](https://i.imgur.com/9p7qmcf.png)
![image](https://i.imgur.com/0IwlGvK.png)
![image](https://i.imgur.com/4CFGKaS.png)



## 헥사고날 아키텍처 다이어그램 도출

* CQRS 를 위한 Mypage 서비스만 DB를 구분하여 적용

![image](https://i.imgur.com/XLlfwa9.png)


# 구현

## 시나리오 테스트결과

**기능  및 이벤트 Payload**

1.카페주인이 카페메뉴(음료이름, 주문가능일, 주문가능수)를 등록한다. 
![image1](https://i.imgur.com/K9PnQQg.png) 
![image1-1](https://i.imgur.com/ZnW4jrz.png)   



2.고객이 주문을 요청한다. ![image2](https://i.imgur.com/SbJDpfy.png)![image2-1](https://i.imgur.com/zBpcT5r.png)     

3.고객의 주문 요청에 따라서 해당 음료의 주문가능 수가 감소한다.(Sync)          ![image3](https://i.imgur.com/HMxakxg.png)
![image3-1](https://i.imgur.com/0Ahto8c.png)  

4.고객의 주문 상태가 주문 완료로 변경된다.(Async) 
![image4](https://i.imgur.com/lgeeVq4.png) 
![image4-1](https://i.imgur.com/U3ttDlk.png) 

5.고객의 주문 완료에 따라서 주문관리의 해당 내역의 상태가 등록된다.
![image5](https://i.imgur.com/eI3dL7o.png)  
![image5-1](https://i.imgur.com/UaL1jcS.png) 

6.고객이 주문을 취소한다.
![image6](https://i.imgur.com/CxNcqJZ.png) 
![image6-1](https://i.imgur.com/TZ9z9mz.png)

7.고객의 주문 취소에 따라서 해당 음료의 주문가능 수가 증가한다.(Async)
![image7](https://i.imgur.com/7JhDVDn.png)
![image7-1](https://i.imgur.com/4ucCnd6.png)  

8.고객의 주문 취소에 따라서 주문관리의 해당 내역 상태가 주문 취소로 변경된다.
![image8](https://i.imgur.com/Dq5Ccsz.png)
![image8-1](https://i.imgur.com/tug7WxB.png) 


9.카페주인이 메뉴 정보를 삭제한다.
![image9](https://i.imgur.com/4DPcuQa.png) 
![image9-1](https://i.imgur.com/oWFcvgD.png)  


10.카페주인의 주문 정보 삭제에 따라서 주문관리의 해당 내역 상태가 강제취소로 변경된다. ![image10](https://i.imgur.com/9Uj2x3q.png) 
![image10-1](https://i.imgur.com/iyUIsyq.png)


11.고객이 주문 진행내역 상태를 조회한다.                         ![image11](https://i.imgur.com/5fV5omd.png) 
![image11-1](https://i.imgur.com/yQOEBsM.png)

## DDD의 적용

분석/설계 단계에서 도출된 MSA는 총 4개로 아래와 같다.
* MyPage 는 CQRS 를 위한 서비스

| MSA | 기능 | port | 조회 API | Gateway 사용시 |
|---|:---:|:---:|---|---|
| Order | 주문 관리 | 8081 | http://localhost:8081/orders | http://OrderManage:8080/orders |
| Cafe  | 카페메뉴 관리 | 8082 | http://localhost:8082/cafes | http://CafeManage:8080/cafes |
| Production | 제조 관리 | 8083 | http://localhost:8083/productions | http://ProductionManage:8080/productions |
| MyPage | my page | 8084 | http://localhost:8084/myPages | http://MyPage:8080/myPages |



## Gateway 적용

```
spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: OrderManage
          uri: http://OrderManage:8080
          predicates:
            - Path=/orders/**
        - id: CafeManage
          uri: http://CafeManage:8080
          predicates:
            - Path=/cafes/** 
        - id: ProductionManage
          uri: http://ProductionManage:8080
          predicates:
            - Path=/productions/** 
        - id: MyPage
          uri: http://MyPage:8080
          predicates:
            - Path= /myPages/**           

```


## 폴리글랏 퍼시스턴스

CQRS 를 위한 Mypage 서비스만 DB를 구분하여 적용함. 인메모리 DB인 hsqldb 사용.

```
pom.xml 에 적용
<!-- 
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
 -->
		<dependency>
		    <groupId>org.hsqldb</groupId>
		    <artifactId>hsqldb</artifactId>
		    <version>2.4.0</version>
		    <scope>runtime</scope>
		</dependency>
```


## 동기식 호출과 Fallback 처리

분석단계에서의 조건 중 하나로 주문요청(Order)->카페메뉴관리(Cafe) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 
주문요청 > 카페메뉴관리 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리
- FeignClient 서비스 구현 

```
# CafeService.java

@FeignClient(name="CafeManage", url="${api.cafe.url}")//,fallback = CafeServiceFallback.class)
public interface CafeService {

    @RequestMapping(method= RequestMethod.PUT, value="/cafes/{cafeId}", consumes = "application/json")
    public void orderRequest(@PathVariable("cafeId") Long cafeId, @RequestBody Cafe cafe);

}
```

- 주문요청을 받은 직후(@PostPersist) 카페메뉴정보를 요청하도록 처리
```
# Order.java

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
    
    
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 카페메뉴관리 시스템이 장애가 나면 주문요청 못받는다는 것을 확인


```
#카페메뉴관리(Cafe) 서비스를 잠시 내려놓음 (ctrl+c)

#신규 주문 요청
http post a8957ed159a9c4f0693659d7848dd3cd-811536622.ap-northeast-1.elb.amazonaws.com:8080/orders custNm="Hwang" cafeId=5 cafeNm="Coldbrew" chkDate="20210713"   #Fail


#카페메뉴관리 재기동
cd CafeManage
mvn spring-boot:run

#주문요청 처리 
http post a8957ed159a9c4f0693659d7848dd3cd-811536622.ap-northeast-1.elb.amazonaws.com:8080/orders custNm="Hwang" cafeId=5 cafeNm="Coldbrew" chkDate="20210713"   #Success   

```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, Fallback 처리는 운영단계에서 설명한다.)



## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트


주문취소가 이루어진 후에 제조관리로 이를 알려주는 행위는 동기식이 아니라 비 동기식으로 처리하였다.
 
- 이를 위하여 주문관리에 기록을 남긴 후에 곧바로 주문취소 되었다는 되었다는 도메인 이벤트를 카프카로 송출한다(Publish)
 
```
#Order.java

@Entity
@Table(name="Order_table")
public class Order {

...

    @PostUpdate
    public void onPostUpdate(){

        System.out.println("#### onPostUpdate :" + this.toString());

        if("CANCELED".equals(this.getStatus())) {
            Canceled canceled = new Canceled();
            BeanUtils.copyProperties(this, canceled);
            canceled.publishAfterCommit();
        }
        
...
    }
```
- 제조관리 서비스에서는 주문취소 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다

```
package local;

@Service
public class PolicyHandler{

@StreamListener(KafkaProcessor.INPUT)
    public void wheneverCanceled_ProductionCancel(@Payload Canceled canceled){

        if(canceled.isMe()){
            //  주문 취소로 인한 취소
            Production temp = productionRepository.findByOrderId(canceled.getId());
            temp.setStatus("CANCELED");
            productionRepository.save(temp);

        }
 ```
제조관리 시스템은 카페메뉴관리/주문관리와 완전히 분리되어있으며, 이벤트 수신에 따라 처리되기 때문에, 제조관리시스템이 유지보수로 인해 잠시 내려간 상태라도 주문을 받는데 문제가 없다.

```
#제조관리 서비스를 잠시 내려놓음 (ctrl+c)

#주문요청 취소 처리 
http put a8957ed159a9c4f0693659d7848dd3cd-811536622.ap-northeast-1.elb.amazonaws.com:8080/orders/2 custNm="Hwang" cafeId=5 cafeNm="Coldbrew" chkDate="20210713" status="CANCELED"   #Success


#제조관리상태 확인
http a8957ed159a9c4f0693659d7848dd3cd-811536622.ap-northeast-1.elb.amazonaws.com:8080/productions      # 제조상태 안바뀜 확인

#제조관리 서비스 기동
cd ProductionManage
mvn spring-boot:run

#제조관리상태 확인 
http a8957ed159a9c4f0693659d7848dd3cd-811536622.ap-northeast-1.elb.amazonaws.com:8080/productions      # 제조상태가 "취소됨"으로 확인
```

# 운영

## CI/CD 설정

각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 AWS CodeBuild를 사용하였으며, 
pipeline build script 는 각 프로젝트 폴더 이하에 buildspec.yml 에 포함되었다.
- CodeBuild 기반으로 CI/CD 파이프라인 구성
MSA 서비스별 CodeBuild 프로젝트 생성하여  CI/CD 파이프라인 구성

![image_repo](https://i.imgur.com/MJwMKq1.png)


- Git Hook 연결
연결한 Github의 소스 변경 발생 시 자동으로 빌드 및 배포 되도록 Git Hook 연결 설정

![image](https://i.imgur.com/LxR4iO4.png)


## 동기식호출 / 서킷브레이킹 / 장애격리

### 서킷 브레이킹 istio-injection + DestinationRule

* istio-injection 적용 (기 적용완료)
```
kubectl label namespace skcc04-ns istio-injection=enabled
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

<수정>
```
$siege -c100 -t60S -r10  -v http://ac8964ea2ef644fb083721500a8e7f07-1903719250.ap-northeast-1.elb.amazonaws.com:8080/cafes 
HTTP/1.1 200     0.15 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.10 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.16 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.11 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.16 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.17 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.16 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.10 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.10 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.12 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.12 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.09 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.11 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.12 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.12 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.10 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.11 secs:     243 bytes ==> GET  /cafes
HTTP/1.1 200     0.11 secs:     243 bytes ==> GET  /cafes
```
```
* 서킷 브레이킹을 위한 DestinationRule 적용
```
```
#dr-cafemanage.yaml  

apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: dr-cafemanage
  namespace: skcc04-ns
spec:
  host: cafemanage
  trafficPolicy:
    connectionPool:
      http:
        http1MaxPendingRequests: 1
        maxRequestsPerConnection: 1
    outlierDetection:
      interval: 1s
      consecutiveErrors: 2
      baseEjectionTime: 10s
      maxEjectionPercent: 100

```
```
$kubectl apply -f dr-cafemanage.yaml

$siege -c100 -t60S -r10  -v http://a753ffdada9fe4c0a8a9d0f885d2b13e-117089432.ap-northeast-1.elb.amazonaws.com:8080/cafes 
HTTP/1.1 200   0.03 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 200   0.04 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 200   0.02 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      81 bytes ==> GET  /cafes
HTTP/1.1 200   0.01 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      81 bytes ==> GET  /cafes
HTTP/1.1 200   0.01 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 200   0.01 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 200   0.01 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 200   0.01 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      81 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      81 bytes ==> GET  /cafes
HTTP/1.1 200   0.02 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 200   0.02 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 200   0.01 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      81 bytes ==> GET  /cafes
HTTP/1.1 503   0.02 secs:      95 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      95 bytes ==> GET  /cafes
HTTP/1.1 200   0.03 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 200   0.02 secs:    5650 bytes ==> GET  /cafes
HTTP/1.1 503   0.00 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.00 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.02 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.02 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.02 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.02 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.02 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.00 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.00 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.02 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.00 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.00 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.00 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.00 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.01 secs:      19 bytes ==> GET  /cafes
HTTP/1.1 503   0.00 secs:      19 bytes ==> GET  /cafes

Transactions:                    194 hits
Availability:                  16.68 %
Elapsed time:                  59.76 secs
Data transferred:               1.06 MB
Response time:                  0.03 secs
Transaction rate:               3.25 trans/sec
Throughput:                     0.02 MB/sec
Concurrency:                    0.10
Successful transactions:         194
Failed transactions:             969
Longest transaction:            0.04
Shortest transaction:           0.00


```

* DestinationRule 적용되어 서킷 브레이킹 동작 확인 (kiali 화면)
![image](https://i.imgur.com/aVctGQF.png)


* 다시 부하 발생하여 DestinationRule 적용 제거하여 정상 처리 확인
```
kubectl delete -f dr-cafemanage.yaml
```


### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

* Metric Server 설치(CPU 사용량 체크를 위해)
```
$kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.3.6/components.yaml
$kubectl get deployment metrics-server -n kube-system
```

* (istio injection 적용한 경우) istio injection 적용 해제
```
kubectl label namespace skcc04-ns istio-injection=disabled --overwrite
```

- Deployment 배포시 resource 설정 적용 -> buildsprc.yml 내 resource 설정 적용
```
    spec:
      containers:
          ...
          resources:
            limits:
              cpu: 500m 
            requests:
              cpu: 200m 
```

- replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:
```
kubectl autoscale deploy cafe -n skcc04-ns --min=1 --max=10 --cpu-percent=15

# 적용 내용
$kubectl get all -n skcc04-ns

NAME                                    READY   STATUS    RESTARTS   AGE
pod/cafemanage-86f7988679-5bzd2         1/2     Running   2          4m52s
pod/cafemanage-86f7988679-74h7q         1/2     Running   0          111s
pod/cafemanage-86f7988679-d4wfv         1/2     Running   0          95s
pod/cafemanage-86f7988679-hzj2l         1/2     Running   0          2m6s
pod/cafemanage-86f7988679-km27b         1/2     Running   0          2m6s
pod/cafemanage-86f7988679-lrz8p         1/2     Running   0          110s
pod/cafemanage-86f7988679-n9rjn         1/2     Running   0          95s
pod/cafemanage-86f7988679-np8br         1/2     Running   2          11m
pod/cafemanage-86f7988679-pd5gd         1/2     Running   0          111s
pod/cafemanage-86f7988679-zgz7w         1/2     Running   0          110s
pod/gateway-78d565b96c-q74nj            2/2     Running   0          150m
pod/mypage-7b48f44d4-9rxv4              1/2     Running   2          149m
pod/ordermanage-56b566f7df-7m45w        1/2     Running   2          90m
pod/productionmanage-7c44d869f5-p7l5f   1/2     Running   2          149m
pod/siege-95579fcc5-gb7kd               1/1     Running   0          29m

NAME                       TYPE           CLUSTER-IP       EXTERNAL-IP                                                                   PORT(S)          AGE
service/cafemanage         ClusterIP      10.100.192.90    <none>                                                                        8080/TCP         149m
service/gateway            LoadBalancer   10.100.150.152   a8957ed159a9c4f0693659d7848dd3cd-811536622.ap-northeast-1.elb.amazonaws.com   8080:32734/TCP   150m
service/mypage             ClusterIP      10.100.48.244    <none>                                                                        8080/TCP         149m
service/ordermanage        ClusterIP      10.100.211.87    <none>                                                                        8080/TCP         90m
service/productionmanage   ClusterIP      10.100.138.63    <none>                                                                        8080/TCP         149m

NAME                               READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/cafemanage         0/10    10           0           149m
deployment.apps/gateway            1/1     1            1           150m
deployment.apps/mypage             0/1     1            0           149m
deployment.apps/ordermanage        0/1     1            0           90m
deployment.apps/productionmanage   0/1     1            0           149m
deployment.apps/siege              1/1     1            1           29m

NAME                                          DESIRED   CURRENT   READY   AGE
replicaset.apps/cafemanage-86f7988679         10        10        0       149m
replicaset.apps/cafemanage-b6995998c          0         0         0       77m
replicaset.apps/gateway-78d565b96c            1         1         1       150m
replicaset.apps/mypage-7b48f44d4              1         1         0       149m
replicaset.apps/ordermanage-56b566f7df        1         1         0       90m
replicaset.apps/productionmanage-7c44d869f5   1         1         0       149m
replicaset.apps/siege-95579fcc5               1         1         1       29m

NAME                                             REFERENCE               TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
cafemanage   Deployment/cafemanage   131%/15%   1         10        10         65m


```

- siege로 워크로드를 1분 동안 걸어준다.
```
$  siege -c100 -t60S -r10  -v http://a8957ed159a9c4f0693659d7848dd3cd-811536622.ap-northeast-1.elb.amazonaws.com:8080/cafes
```

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy cafemanage -n skcc04-ns -w 
```

- 스케일 아웃 결과
```
NAME         READY   UP-TO-DATE   AVAILABLE   AGE
cafemanage   1/1     1            1           142m
cafemanage   1/2     1            1           144m
cafemanage   1/2     1            1           144m
cafemanage   1/2     1            1           144m
cafemanage   1/2     2            1           144m
cafemanage   0/2     2            0           145m
cafemanage   0/4     2            0           147m
cafemanage   0/4     2            0           147m
cafemanage   0/4     2            0           147m
cafemanage   0/4     4            0           147m
cafemanage   0/8     4            0           147m
cafemanage   0/8     4            0           147m
cafemanage   0/8     4            0           147m
cafemanage   0/8     8            0           147m
cafemanage   0/10    8            0           147m
cafemanage   0/10    8            0           147m
cafemanage   0/10    8            0           147m
cafemanage   0/10    10           0           147m
```

- kubectl get으로 HPA을 확인하면 CPU 사용률이 131%로 증가됐다.
```
$kubectl get hpa cafemanage -n skcc04-ns 
NAME         REFERENCE               TARGETS    MINPODS   MAXPODS   REPLICAS   AGE
cafemanage   Deployment/cafemanage   131%/15%   1         10        10         65m
```

- siege 의 로그를 보면 Availability가 100%로 유지된 것을 확인 할 수 있다.  
```
Lifting the server siege...
Transactions:                  26446 hits
Availability:                 100.00 %
Elapsed time:                 179.76 secs
Data transferred:               8.73 MB
Response time:                  0.68 secs
Transaction rate:             147.12 trans/sec
Throughput:                     0.05 MB/sec
Concurrency:                   99.60
Successful transactions:       26446
Failed transactions:               0
Longest transaction:            5.85
Shortest transaction:           0.00
```

- HPA 삭제 
```
$kubectl delete hpa cafemanage -n skcc04-ns
```


## 무정지 재배포

먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler, CB 설정을 제거함
Readiness Probe 미설정 시 무정지 재배포 가능여부 확인을 위해 buildspec.yml의 Readiness Probe 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
$ siege -v -c1 -t240S --content-type "application/json" 'http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes POST {"cafeId":"99","cafeNm":"coffee","chkDate":"210713","pcnt":20}'

** SIEGE 4.0.4
** Preparing 100 concurrent users for battle.
The server is now under siege...

HTTP/1.1 200     0.08 secs:       0 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 200     0.09 secs:       0 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 200     0.06 secs:       0 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 200     0.08 secs:       0 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 200     0.08 secs:       0 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 200     0.07 secs:       0 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes

```

- CI/CD 파이프라인을 통해 새버전으로 재배포 작업함
Git hook 연동 설정되어 Github의 소스 변경 발생 시 자동 빌드 배포됨
재배포 작업 중 서비스 중단됨 (503 오류 발생)
```
HTTP/1.1 200     0.48 secs:       0 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 200     0.55 secs:       0 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 503     0.47 secs:      95 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 503     0.48 secs:      95 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 503     0.51 secs:      95 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 503     0.47 secs:      95 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 503     0.48 secs:      95 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 503     0.53 secs:      95 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 503     0.50 secs:      95 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
HTTP/1.1 503     0.45 secs:      95 bytes ==> POST http://a0a49bc9c3b964b96bf740b592da2520-1468765953.ap-northeast-1.elb.amazonaws.com:8080/cafes
:

```

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인
```
Transactions:                    372 hits
Availability:                  90.29 %
Elapsed time:                 205.09 secs
Data transferred:               0.00 MB
Response time:                  0.55 secs
Transaction rate:               1.81 trans/sec
Throughput:                     0.00 MB/sec
Concurrency:                    1.00
Successful transactions:         372
Failed transactions:              40
Longest transaction:            1.50
Shortest transaction:           0.43

```
- 배포기간중 Availability 가 평소 100%에서 90% 대로 떨어지는 것을 확인. 
원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문으로 판단됨. 
이를 막기위해 Readiness Probe 를 설정함 (buildspec.yml의 Readiness Probe 설정)
```
# buildspec.yaml 의 Readiness probe 의 설정:
- CI/CD 파이프라인을 통해 새버전으로 재배포 작업함

readinessProbe:
    httpGet:
      path: '/actuator/health'
      port: 8080
    initialDelaySeconds: 30
    timeoutSeconds: 2
    periodSeconds: 5
    failureThreshold: 10
    
```

- 동일한 시나리오로 재배포 한 후 Availability 확인:
```
Lifting the server siege...
Transactions:                   3029 hits
Availability:                 100.00 %
Elapsed time:                 239.62 secs
Data transferred:               0.00 MB
Response time:                  0.08 secs
Transaction rate:              12.64 trans/sec
Throughput:                     0.00 MB/sec
Concurrency:                    1.00
Successful transactions:        3029
Failed transactions:               0
Longest transaction:            0.45
Shortest transaction:           0.06

```

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.


## ConfigMap 사용

시스템별로 또는 운영중에 동적으로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리합니다.
Application에서 특정 도메일 URL을 ConfigMap 으로 설정하여 운영/개발등 목적에 맞게 변경가능합니다.  

* my-config.yaml
```
apiVersion: v1
kind: ConfigMap
metadata:
name: my-config
namespace: skcc04-ns
data:
api.cafe.url: http://CafeManage:8080

```
buildspec.yaml에 my-config라는 CongifMap을 생성하고, key 값에 도메인 url을 등록한다. 

* OrderManage/buildsepc.yaml (configmap 사용)
```
        ...
        cat <<EOF | kubectl apply -f -
        apiVersion: v1
        kind: ConfigMap
        metadata:
          name: my-config
          namespace: $_NAMESPACE
        data:
          api.cafe.url: http://CafeManage:8080
        EOF
      - |
        cat  <<EOF | kubectl apply -f -
        apiVersion: apps/v1
        kind: Deployment
        metadata:
          name: $_PROJECT_NAME
          namespace: $_NAMESPACE
          labels:
            app: $_PROJECT_NAME
        spec:
          replicas: 1
          selector:
            matchLabels:
              app: $_PROJECT_NAME
          template:
            metadata:
              labels:
                app: $_PROJECT_NAME
            spec:
              containers:
                - name: $_PROJECT_NAME
                  image: $AWS_ACCOUNT_ID.dkr.ecr.$_AWS_REGION.amazonaws.com/$_ECR_NAME-$_PROJECT_NAME:latest
                  ports:
                    - containerPort: 8080
                  env:
                    - name: api.cafe.url
                      valueFrom:
                        configMapKeyRef:
                          name: my-config
                          key: api.cafe.url
                  imagePullPolicy: Always
                  ...
```
Deployment yaml에 해당 configMap 적용

* CafeService.java
```
@FeignClient(name="CafeManage", url="${api.cafe.url}")//,fallback = CafeServiceFallback.class)
public interface CafeService {

    @RequestMapping(method= RequestMethod.PUT, value="/cafes/{cafeId}", consumes = "application/json")
    public void orderRequest(@PathVariable("cafeId") Long cafeId, @RequestBody Cafe cafe);

}
```
url에 configMap 적용

* kubectl describe pod ordermanage-74c7d468df -n skcc04-ns
```
Containers:
  ordermanage:
    Container ID:   docker://c2bf8e39767b9f6767e80d31642cac85ad9df6ff2f40ea84948ef092a1c99c7d
    Image:          879772956301.dkr.ecr.ap-northeast-1.amazonaws.com/user04-ordermanage:latest
    Image ID:       docker-pullable://879772956301.dkr.ecr.ap-northeast-1.amazonaws.com/user04-ordermanage@sha256:d09e8bd588423c698e0466e90263c8ba43770472425ebd562c589fcd9ddf84a7
    Port:           8080/TCP
    Host Port:      0/TCP
    State:          Running
      Started:      Tue, 13 Jul 2021 03:42:21 +0000
    Ready:          True
    Restart Count:  0
    Liveness:       http-get http://:15020/app-health/ordermanage/livez delay=150s timeout=2s period=5s #success=1 #failure=5
    Readiness:      http-get http://:15020/app-health/ordermanage/readyz delay=30s timeout=2s period=5s #success=1 #failure=10
    Environment:
      api.cafe.url:  <set to the key 'api.cafe.url' of config map 'my-config'>  Optional: false
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from default-token-j7j95 (ro)
```
kubectl describe 명령으로 컨테이너에 configMap 적용여부를 알 수 있다. 

