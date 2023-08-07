package ssafy.project.jobterview.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ssafy.project.jobterview.domain.Mate;
import ssafy.project.jobterview.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MateRepository extends JpaRepository<Mate, Member>, MateCustomRepository {

    List<Mate> findByFromMember(Member fromMember);

    Page<Mate> findByFromMember(Member fromMember,Pageable pageable);

    Optional<Mate> findByFromMemberAndToMember(Member fromMember, Member toMember);
}