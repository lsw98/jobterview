package ssafy.project.jobterview.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.project.jobterview.domain.Bookmark;
import ssafy.project.jobterview.domain.Member;
import ssafy.project.jobterview.domain.Room;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByMember(Member member, Pageable pageable);

    Bookmark findByMemberAndRoom(Member member, Room room);
}
