create table QuitSlips (
    id integer primary key autoincrement,
    habit integer not null references habits(id),
    timestamp integer not null
);
create unique index idx_quitslips_habit_timestamp on QuitSlips(habit, timestamp);
insert or ignore into QuitSlips(habit, timestamp)
    select habit, timestamp + 86340000 from Repetitions
    where value = 0 and habit in (select id from Habits where type = 2);
delete from Repetitions
    where value = 0 and (notes is null or notes = '')
    and habit in (select id from Habits where type = 2);
update Repetitions set value = -1
    where value = 0 and habit in (select id from Habits where type = 2);
