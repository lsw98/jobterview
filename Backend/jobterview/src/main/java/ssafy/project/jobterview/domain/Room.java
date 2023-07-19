package ssafy.project.jobterview.domain;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import ssafy.project.jobterview.dto.RoomDto;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class Room extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @OneToMany(mappedBy = "room")
    private List<Chat> roomChatList = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255) default 'WAITING'")
    private RoomStatus status;

    @Column(name = "now_member", nullable = false)
    @ColumnDefault("1")
    private int nowMember;

    @Column(name = "max_member", nullable = false)
    @ColumnDefault("1")
    private int maxMember;

    @Column(name = "room_name")
    private String roomName;

    public Room(String roomName, int maxMember) {
        this.roomName = roomName;
        this.maxMember = maxMember;
    }

    /**
     * Room 객체를 RoomDto 형태로 변환
     * 
     * @return 변환된 RoomDto 객체
     */
    public RoomDto convertToDto() {
        return RoomDto.builder()
                .roomId(this.getRoomId())
                .roomChatList(this.getRoomChatList())
                .roomName(this.getRoomName())
                .nowMember(this.getNowMember())
                .maxMember(this.getMaxMember())
                .roomStatus(this.getStatus())
                .createdDate(this.getCreatedDate())
                .build();
    }

    public void startMeeting() {
        this.status = RoomStatus.MEETING;
    }

    public void waitMeeting() {
        this.status = RoomStatus.WAITING;
    }

    public void close() {
        this.status = RoomStatus.ENDED;
    }
}
