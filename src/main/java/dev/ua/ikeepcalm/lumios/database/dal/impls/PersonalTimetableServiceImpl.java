package dev.ua.ikeepcalm.lumios.database.dal.impls;

import dev.ua.ikeepcalm.lumios.database.dal.interfaces.PersonalTimetableService;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable.ElectiveChoiceRepository;
import dev.ua.ikeepcalm.lumios.database.dal.repositories.timetable.TimetableMemberRepository;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.ElectiveChoice;
import dev.ua.ikeepcalm.lumios.database.entities.timetable.personal.TimetableMember;
import dev.ua.ikeepcalm.lumios.telegram.utils.TimetableClock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonalTimetableServiceImpl implements PersonalTimetableService {

    private final TimetableMemberRepository memberRepository;
    private final ElectiveChoiceRepository choiceRepository;

    public PersonalTimetableServiceImpl(TimetableMemberRepository memberRepository,
                                        ElectiveChoiceRepository choiceRepository) {
        this.memberRepository = memberRepository;
        this.choiceRepository = choiceRepository;
    }

    @Override
    @Transactional
    public TimetableMember member(Long chatId, Long telegramUserId) {
        return memberRepository.findByChatIdAndTelegramUserId(chatId, telegramUserId)
                .orElseGet(() -> {
                    TimetableMember member = new TimetableMember();
                    member.setChatId(chatId);
                    member.setTelegramUserId(telegramUserId);
                    member.setReviewedAt(TimetableClock.nowDateTime());
                    return memberRepository.save(member);
                });
    }

    @Override
    public List<TimetableMember> remindableMembers(Long chatId) {
        return memberRepository.findByChatIdAndDmRemindersEnabledTrueAndDmUnavailableFalse(chatId);
    }

    @Override
    public List<TimetableMember> membershipsOf(Long telegramUserId) {
        return memberRepository.findByTelegramUserId(telegramUserId);
    }

    @Override
    @Transactional
    public void save(TimetableMember member) {
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void markDmUnavailable(Long chatId, Long telegramUserId) {
        TimetableMember member = member(chatId, telegramUserId);
        member.setDmUnavailable(true);
        memberRepository.save(member);
    }

    @Override
    public Set<String> chosenSubjects(Long chatId, Long telegramUserId) {
        return choiceRepository.findByChatIdAndTelegramUserId(chatId, telegramUserId).stream()
                .map(ElectiveChoice::getSubjectKey)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public boolean toggleChoice(Long chatId, Long telegramUserId, String subjectKey) {
        if (choiceRepository.existsByChatIdAndTelegramUserIdAndSubjectKey(chatId, telegramUserId, subjectKey)) {
            choiceRepository.deleteByChatIdAndTelegramUserIdAndSubjectKey(chatId, telegramUserId, subjectKey);
            return false;
        }
        ElectiveChoice choice = new ElectiveChoice();
        choice.setChatId(chatId);
        choice.setTelegramUserId(telegramUserId);
        choice.setSubjectKey(subjectKey);
        choiceRepository.save(choice);
        return true;
    }

    @Override
    @Transactional
    public int pruneChoices(Long chatId, Set<String> validSubjectKeys) {
        Set<String> valid = new HashSet<>(validSubjectKeys);
        List<ElectiveChoice> stale = choiceRepository.findByChatId(chatId).stream()
                .filter(choice -> !valid.contains(choice.getSubjectKey()))
                .toList();
        choiceRepository.deleteAll(stale);
        return stale.size();
    }

}
