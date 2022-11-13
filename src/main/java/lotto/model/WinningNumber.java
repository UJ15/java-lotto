package lotto.model;

import static lotto.Application.ERROR_PREFIX;
import static lotto.engine.NextstepNumberGenerator.COUNT;
import static lotto.engine.NextstepNumberGenerator.END_RANGE_NUMBER;
import static lotto.engine.NextstepNumberGenerator.START_RANGE_NUMBER;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WinningNumber {
    private static final String NUMBER_INPUT_REGEX = "^(\\d{1,2},){5}(\\d{1,2}$)";
    private static final String BONUS_INPUT_REGEX = "^(\\d{1,2})$";
    private static final String EMPTY_INPUT_ERROR_MESSAGE =
            ERROR_PREFIX + "번호를 입력하세요.";
    private static final String INPUT_FORMAT_ERROR_MESSAGE =
            ERROR_PREFIX + "당첨 숫자는 1~45 숫자를 '0,0,0,0,0,0' 형식으로 입력해주세요.";
    private static final String BONUS_FORMAT_ERROR_MESSAGE =
            ERROR_PREFIX + "보너스 숫자는 1~45 숫자를 당첨 숫자와 중복되지 않게 입력해주세요";

    private final List<Integer> numbers;
    private final int bonusNumber;

    private WinningNumber(List<Integer> numbers, int bonusNumber) {
        this.numbers = numbers;
        this.bonusNumber = bonusNumber;
    }

    public Optional<Winning> compareToWinningNumber(Lotto lotto) {
        if (lotto == null) {
            throw new IllegalArgumentException();
        }
        AtomicInteger correctNumber = new AtomicInteger();
        AtomicBoolean bonus = new AtomicBoolean();

        lotto.iterate(n -> {
            if (numbers.contains(n)) {
                correctNumber.getAndIncrement();
            }
            if (n == bonusNumber) {
                bonus.getAndSet(true);
            }
        });

        return getWinning(correctNumber.get(), bonus.get());
    }

    private Optional<Winning> getWinning(int correctNumber, boolean bonus) {
        switch (correctNumber) {
            case 3:
                return Optional.of(Winning.THREE);
            case 4:
                return Optional.of(Winning.FOUR);
            case 5:
                if (bonus) {
                    return Optional.of(Winning.FIVE_BONUS);
                }
                return Optional.of(Winning.FIVE);
            case 6:
                return Optional.of(Winning.SIX);
            default:
                return Optional.empty();
        }
    }

    public static WinningNumber of(String userInputNumber, String userInputBonusNumber) {
        if (userInputNumber == null
                || userInputBonusNumber == null
                || userInputBonusNumber.isEmpty()
                || userInputNumber.isEmpty()
        ) {
            throw new IllegalArgumentException(EMPTY_INPUT_ERROR_MESSAGE);
        }

        List<Integer> numbers = parseToNumbers(userInputNumber);
        int bonusNumber = parseToBonusNumber(userInputBonusNumber, numbers);

        return new WinningNumber(numbers, bonusNumber);
    }

    private static List<Integer> parseToNumbers(String userInputNumber) {
        if (!userInputNumber.matches(NUMBER_INPUT_REGEX)) {
            throw new IllegalArgumentException(INPUT_FORMAT_ERROR_MESSAGE);
        }

        List<Integer> numbers = Arrays.stream(userInputNumber.split(","))
                .map(Integer::parseInt)
                .filter(n -> n >= START_RANGE_NUMBER && n <= END_RANGE_NUMBER)
                .collect(Collectors.toList());

        if (numbers.size() < COUNT) {
            throw new IllegalArgumentException(INPUT_FORMAT_ERROR_MESSAGE);
        }

        return numbers;
    }

    private static int parseToBonusNumber(String userInputBonusNumber, List<Integer> numbers) {
        if (!userInputBonusNumber.matches(BONUS_INPUT_REGEX)) {
            throw new IllegalArgumentException(BONUS_FORMAT_ERROR_MESSAGE);
        }

        int bonusNumber = Integer.parseInt(userInputBonusNumber);

        if (bonusNumber > END_RANGE_NUMBER || bonusNumber < START_RANGE_NUMBER || numbers.contains(bonusNumber)) {
            throw new IllegalArgumentException(BONUS_FORMAT_ERROR_MESSAGE);
        }

        return bonusNumber;
    }
}
