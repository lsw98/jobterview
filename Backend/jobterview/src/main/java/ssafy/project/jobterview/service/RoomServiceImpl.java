package ssafy.project.jobterview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.project.jobterview.domain.Room;
import ssafy.project.jobterview.dto.RoomDto;
import ssafy.project.jobterview.exception.NotFoundException;
import ssafy.project.jobterview.repository.RoomQueryRepository;
import ssafy.project.jobterview.repository.RoomRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final RoomQueryRepository roomQueryRepository;


    /*@Override
    public Room save(Room room) {
        String roomName = room.getRoomName();
        Optional<Room> findRoom = roomRepository.findByRoomName(roomName);

        if(findRoom.isEmpty()) {
            return roomRepository.save(room);
        }
        throw new IllegalArgumentException("해당 이름의 방이 이미 존재합니다.");
    }*/

    /**
     * 특정 roomId에 해당하는 Room 정보 조회
     *
     * @param roomId 조회할 Room의 식별자
     * @return ResponseEntity<RoomDto> 형태로 조회된 Room 정보 반환
     */
    @Override
    public Room findById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("해당 id의 방이 존재하지 않습니다."));
    }

    /**
     * 특정 roomName에 해당하는 Room 목록 조회
     *
     * @param roomName 조회할 Room의 이름
     * @return ResponseEntity<RoomDto> 형태로 조회된 Room 정보 반환
     */
    @Override
    public Room findByName(String roomName) {
        return  roomRepository.findByRoomName(roomName)
                .orElseThrow(() -> new NotFoundException("해당 이름의 방이 존재하지 않습니다."));
    }

    /**
     * 특정 keyword를 roomName에 포함하는 Room 목록 조회
     *
     * @param keyword roomName이 포함하는지 조회할 검색어
     * @param pageable 페이징 및 정렬 정보
     * @return ResponseEntity<Page<RoomDto>> 형태로 페이징된 Room 목록 반환
     */
    @Override
    public Page<Room> searchByName(String keyword, Pageable pageable) {
        return roomQueryRepository.searchByName(keyword, pageable);
    }

    /**
     * 페이징된 Room 목록 조회
     *
     * @param pageable 페이징 및 정렬 정보
     * @return ResponseEntity<Page<RoomDto>> 형태로 페이징된 Room 목록 반환
     */
    @Override
    public Page<Room> findAll(Pageable pageable) throws NotFoundException{
        Page<Room> roomList = roomRepository.findAll(pageable);
        if(roomList.isEmpty()) {
            throw new NotFoundException("생성된 방이 존재하지 않습니다.");
        }
        //Page<Room>을 Page<RoomDto> 형태로 변환 후 반환
        return roomList;
    }
}