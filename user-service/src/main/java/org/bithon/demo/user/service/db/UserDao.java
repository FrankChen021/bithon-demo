/*
 *    Copyright 2020 bithon.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.bithon.demo.user.service.db;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author frank.chen021@outlook.com
 * @date 2021/3/21 20:20
 */
@Slf4j
@Component
public class UserDao {

    public UserDao(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    static class TestTable extends TableImpl {
        private final Field<Long> id;

        public TestTable() {
            super(DSL.name("bithon-demo-db"));
            id = this.createField(DSL.name("id"), SQLDataType.BIGINT);
        }
    }

    private final DSLContext dslContext;
    private final TestTable testTable = new TestTable();
    private long id = 0;

    @PostConstruct
    void init() {
        Table t = new TableImpl<>("bithon-demo-db");
        dslContext.createTemporaryTableIfNotExists(testTable)
                  .columns(testTable.id)
                  .execute();
    }

    @Scheduled(fixedDelay = 1000)
    void insertOperation() {
        log.info("Insert into db...");
        dslContext.insertInto(testTable)
                  .set(testTable.id, id++)
                  .execute();
    }

    @Scheduled(fixedDelay = 2000)
    void updateOperation() {
        log.info("Updating db...");
        dslContext.update(testTable)
                  .set(testTable.id, 0)
                  .where(testTable.id.lt(id))
                  .execute();
    }

    @Scheduled(fixedDelay = 3000)
    void deleteOperation() {
        log.info("Deleting from db...");
        dslContext.delete(testTable)
                  .where(testTable.id.eq(0L))
                  .execute();
    }

    @Scheduled(fixedDelay = 2000)
    void selectOperation() {
        log.info("Selecting from db...");
        dslContext.fetchCount(dslContext.selectFrom(testTable));
    }
}
