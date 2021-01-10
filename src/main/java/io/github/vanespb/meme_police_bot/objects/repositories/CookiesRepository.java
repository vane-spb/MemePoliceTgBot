package io.github.vanespb.meme_police_bot.objects.repositories;

import io.github.vanespb.meme_police_bot.objects.models.CookieModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CookiesRepository extends JpaRepository<CookieModel, Integer> {
}
