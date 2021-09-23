insert into person (created_by, created_date, last_update, updated_by, email, password, username, level_id, status_id) values
('APPLICATION_DEFAULT_USER', now(), now(), 'APPLICATION_DEFAULT_USER', 'catzfield00@gmail.com', '$2y$10$btD4.35HEgEC8LSnriCyU.CHw30iLCfwyUOecVWTTvgM3oKLBJi5y', 'admin origin', 1,3);

insert into person_role(person_id, role_id) values
(2,0);
