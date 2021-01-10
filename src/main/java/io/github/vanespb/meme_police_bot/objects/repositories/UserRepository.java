package io.github.vanespb.meme_police_bot.objects.repositories;

import io.github.vanespb.meme_police_bot.objects.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Integer> {
    Optional<UserModel> getOneByTgNickname(String tgNickname);
}
