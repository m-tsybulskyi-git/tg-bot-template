package ua.mtsybulskyi.template.data;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

@Component
public class FileKey {
    final private String key1Path = "src/main/resources/static/docs/botKey.txt";
    final private String key2Path = "src/main/resources/static/docs/userKey.txt";

    private final int length = 128;

    public int getLength() {
        return length;
    }

    private String keyGenerator(int length) {
        String capitalCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String specialCharacters = "!@#$";
        String numbers = "1234567890";
        String combinedChars = capitalCaseLetters + lowerCaseLetters + specialCharacters + numbers;
        Random random = new Random();
        char[] password = new char[length];

        password[0] = lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length()));
        password[1] = capitalCaseLetters.charAt(random.nextInt(capitalCaseLetters.length()));
        password[2] = specialCharacters.charAt(random.nextInt(specialCharacters.length()));
        password[3] = numbers.charAt(random.nextInt(numbers.length()));

        for (int i = 4; i < length; i++) {
            password[i] = combinedChars.charAt(random.nextInt(combinedChars.length()));
        }

        return String.valueOf(password);
    }

    @SneakyThrows
    public void updateAdminTextKey() {
        String key = keyGenerator(length);
        File file = ResourceUtils.getFile(key1Path);

        Path path = file.toPath();
        Files.write(path, key.getBytes());
    }

    @SneakyThrows
    public boolean compareAdminKeys() {
        Path pathKey1 = new File(key1Path).toPath();
        Path pathKey2 = new File(key2Path).toPath();

        String key1 = Files.readString(pathKey1);
        String key2 = Files.readString(pathKey2);

        if (key1.equals(key2)) {
            updateAdminTextKey();
            return true;
        }

        return false;
    }
}
