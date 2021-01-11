CREATE TABLE users
(   id SERIAL PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE,
    tg_nickname varchar(100) NOT NULL UNIQUE,
    vk_id int UNIQUE
);

INSERT INTO users (id, name, tg_nickname, vk_id) VALUES
(1, 'Тигр', 'vane_n', 31224679),
(2,' Ростик', 'eatmo', 14328069),
(3,' Леня', 'leo_swift', 62610070),
(4,' Руслан', 'Suslikadze', 306997830),
(5,' Вован_любитель_сутулых_собак', 'dushupythona', 30269229),
(6,' Рома', 'Onyborak', 9671586),
(7,' Костя', 'opiakos', 4796205);
