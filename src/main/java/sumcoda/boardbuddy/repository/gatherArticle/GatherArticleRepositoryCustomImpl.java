package sumcoda.boardbuddy.repository.gatherArticle;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import sumcoda.boardbuddy.dto.GatherArticleResponse;
import sumcoda.boardbuddy.entity.QMemberGatherArticle;
import sumcoda.boardbuddy.enumerate.MemberGatherArticleRole;
import sumcoda.boardbuddy.entity.Member;
import sumcoda.boardbuddy.enumerate.GatherArticleStatus;
import sumcoda.boardbuddy.enumerate.ParticipationApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static sumcoda.boardbuddy.entity.QGatherArticle.gatherArticle;
import static sumcoda.boardbuddy.entity.QMember.member;
import static sumcoda.boardbuddy.entity.QMemberGatherArticle.memberGatherArticle;
import static sumcoda.boardbuddy.entity.QProfileImage.profileImage;

@Slf4j
@RequiredArgsConstructor
public class GatherArticleRepositoryCustomImpl implements GatherArticleRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GatherArticleResponse.GatherArticleInfosDTO> findGatherArticleInfosByUsername(String username) {
        return jpaQueryFactory.select(Projections.fields(GatherArticleResponse.GatherArticleInfosDTO.class,
                        gatherArticle.id,
                        gatherArticle.title,
                        gatherArticle.description,
                        gatherArticle.meetingLocation,
                        gatherArticle.maxParticipants,
                        gatherArticle.currentParticipants,
                        gatherArticle.startDateTime,
                        gatherArticle.endDateTime,
                        gatherArticle.createdAt,
                        gatherArticle.gatherArticleStatus.as("status")
                ))
                .from(member)
                .join(member.memberGatherArticles, memberGatherArticle)
                .join(memberGatherArticle.gatherArticle, gatherArticle)
                .where(member.username.eq(username)
                        .and(memberGatherArticle.memberGatherArticleRole.eq(MemberGatherArticleRole.AUTHOR)))
                .fetch();
    }

    @Override
    public List<GatherArticleResponse.GatherArticleInfosDTO> findParticipationsByUsername(String username) {
        return jpaQueryFactory.select(Projections.fields(GatherArticleResponse.GatherArticleInfosDTO.class,
                        gatherArticle.id,
                        gatherArticle.title,
                        gatherArticle.description,
                        gatherArticle.meetingLocation,
                        gatherArticle.maxParticipants,
                        gatherArticle.currentParticipants,
                        gatherArticle.startDateTime,
                        gatherArticle.endDateTime,
                        gatherArticle.createdAt,
                        gatherArticle.gatherArticleStatus.as("status")
                ))
                .from(member)
                .join(member.memberGatherArticles, memberGatherArticle)
                .join(memberGatherArticle.gatherArticle, gatherArticle)
                .where(member.username.eq(username)
                        .and(memberGatherArticle.memberGatherArticleRole.eq(MemberGatherArticleRole.PARTICIPANT)))
                .fetch();
    }

    @Override
    public Boolean isMemberAuthorOfGatherArticle(Long gatherArticleId, String username) {
        return jpaQueryFactory
                .selectOne()
                .from(memberGatherArticle)
                .leftJoin(memberGatherArticle.gatherArticle, gatherArticle)
                .leftJoin(memberGatherArticle.member, member)
                .where(gatherArticle.id.eq(gatherArticleId)
                        .and(member.username.eq(username)
                                .and(memberGatherArticle.memberGatherArticleRole.eq(MemberGatherArticleRole.AUTHOR))))
                .fetchOne() != null;
    }

    // 지난 달에 쓴 모집글 갯수 세기
    @Override
    public long countGatherArticlesByMember(Member member, LocalDateTime startOfLastMonth, LocalDateTime endOfLastMonth) {
        return jpaQueryFactory.select(memberGatherArticle.count())
                .from(memberGatherArticle)
                .where(memberGatherArticle.member.eq(member)
                        .and(memberGatherArticle.memberGatherArticleRole.eq(MemberGatherArticleRole.AUTHOR))
                        .and(memberGatherArticle.gatherArticle.createdAt.between(startOfLastMonth, endOfLastMonth)))
                .fetchOne();
    }

    @Override
    public Optional<GatherArticleResponse.IdDTO> findIdDTOById(Long gatherArticleId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(GatherArticleResponse.IdDTO.class,
                        gatherArticle.id))
                .from(gatherArticle)
                .where(gatherArticle.id.eq(gatherArticleId))
                .fetchOne());
    }

    @Override
    public Slice<GatherArticleResponse.ReadSliceDTO> findReadSliceDTOByLocationAndStatusAndSort(
            List<String> sidoList, List<String> sggList, List<String> emdList, String status, String sort, MemberGatherArticleRole role, Pageable pageable) {

        List<GatherArticleResponse.ReadSliceDTO> results = jpaQueryFactory.select(Projections.fields(
                        GatherArticleResponse.ReadSliceDTO.class,
                        gatherArticle.id,
                        gatherArticle.title,
                        gatherArticle.description,
                        Projections.fields(GatherArticleResponse.AuthorSimpleDTO.class,
                                member.nickname.as("nickname"),
                                member.rank.as("rank")).as("author"),
                        gatherArticle.meetingLocation,
                        gatherArticle.maxParticipants,
                        gatherArticle.currentParticipants,
                        gatherArticle.startDateTime,
                        gatherArticle.endDateTime,
                        gatherArticle.createdAt,
                        gatherArticle.gatherArticleStatus.as("status")))
                .from(gatherArticle)
                .join(gatherArticle.memberGatherArticles, memberGatherArticle)
                .join(memberGatherArticle.member, member)
                .where(
                        inLocation(sidoList, sggList, emdList),
                        eqStatus(status),
                        eqMemberGatherArticleRole(role)
                )
                .orderBy(getOrderSpecifier(sort))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;

        if (results.size() > pageable.getPageSize()) {
            results.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }

    /**
     * 특정 모집글 Id로 간단한 모집글 정보 조회
     *
     * @param gatherArticleId 모집글 Id
     * @return 모집글 정보가 포함된 SummaryInfoDTO 객체
     **/
    @Override
    public Optional<GatherArticleResponse.SummaryInfoDTO> findSimpleInfoByGatherArticleId(Long gatherArticleId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.fields(GatherArticleResponse.SummaryInfoDTO.class,
                        gatherArticle.title,
                        gatherArticle.meetingLocation,
                        gatherArticle.maxParticipants,
                        gatherArticle.currentParticipants,
                        gatherArticle.startDateTime,
                        gatherArticle.endDateTime
                ))
                .from(gatherArticle)
                .where(gatherArticle.id.eq(gatherArticleId))
                .fetchOne());
    }

    private BooleanExpression eqMemberGatherArticleRole(MemberGatherArticleRole role) {
        return memberGatherArticle.memberGatherArticleRole.eq(role);
    }

    private BooleanExpression eqStatus(String status) {
        return status != null ? gatherArticle.gatherArticleStatus.eq(GatherArticleStatus.valueOf(status.toUpperCase())) : null;
    }

    private BooleanExpression inLocation(List<String> sidoList, List<String> sggList, List<String> emdList) {
        return gatherArticle.sido.in(sidoList)
                .and(gatherArticle.sgg.in(sggList))
                .and(gatherArticle.emd.in(emdList));
    }

    private OrderSpecifier<?> getOrderSpecifier(String sort) {
        if (GatherArticleStatus.SOON.getValue().equals(sort)) {
            return gatherArticle.startDateTime.asc();
        } else {
            return gatherArticle.id.desc();
        }
    }

    @Override
    public GatherArticleResponse.ReadDTO findGatherArticleReadDTOByGatherArticleId(Long gatherArticleId, Long memberId) {

        // 현재 사용자의 참여 상태를 위한 서브쿼리
        QMemberGatherArticle currentUserParticipation = new QMemberGatherArticle("currentUserParticipation");

        return jpaQueryFactory
                .select(Projections.fields(
                        GatherArticleResponse.ReadDTO.class,
                        gatherArticle.title,
                        gatherArticle.description,
                        Projections.fields(GatherArticleResponse.AuthorDTO.class,
                                member.nickname.as("nickname"),
                                member.rank.as("rank"),
                                member.profileImage.profileImageS3SavedURL.as("profileImageS3SavedURL"),
                                member.description.as("description")
                        ).as("author"),
                        gatherArticle.sido,
                        gatherArticle.sgg,
                        gatherArticle.emd,
                        gatherArticle.meetingLocation,
                        gatherArticle.x,
                        gatherArticle.y,
                        gatherArticle.maxParticipants,
                        gatherArticle.currentParticipants,
                        gatherArticle.startDateTime,
                        gatherArticle.endDateTime,
                        gatherArticle.createdAt,
                        gatherArticle.gatherArticleStatus.as("status"),
                        new CaseBuilder()
                                .when(currentUserParticipation.isNull())
                                .then(Expressions.constant(ParticipationApplicationStatus.NONE))
                                .when(currentUserParticipation.participationApplication.isNull())
                                .then(Expressions.constant(ParticipationApplicationStatus.NONE))
                                .otherwise(currentUserParticipation.participationApplication.participationApplicationStatus).as("participationApplicationStatus")
                ))
                .from(gatherArticle)
                .leftJoin(gatherArticle.memberGatherArticles, memberGatherArticle)
                .leftJoin(memberGatherArticle.member, member)
                .leftJoin(member.profileImage, profileImage)
                .leftJoin(currentUserParticipation).on(currentUserParticipation.gatherArticle.eq(gatherArticle).and(currentUserParticipation.member.id.eq(memberId)))
                .where(gatherArticle.id.eq(gatherArticleId)
                        .and(memberGatherArticle.memberGatherArticleRole.eq(MemberGatherArticleRole.AUTHOR))
                        .and(memberGatherArticle.member.eq(member)))
                .fetchOne();
    }
}
