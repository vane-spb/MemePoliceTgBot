CREATE TABLE chat_linking
(   id SERIAL PRIMARY KEY,
    tg_chat_id bigint NOT NULL,
    vk_chat_id int NOT NULL
);