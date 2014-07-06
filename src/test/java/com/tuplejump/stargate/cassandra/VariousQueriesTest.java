package com.tuplejump.stargate.cassandra;

import com.tuplejump.stargate.util.CQLUnitD;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * User: satya
 */
public class VariousQueriesTest extends IndexTestBase {
    String keyspace = "dummyksLang";

    public VariousQueriesTest() {
        cassandraCQLUnit = CQLUnitD.getCQLUnit(null);
    }

    @Test
    public void shouldIndexPerRow() throws Exception {
        //hack to always create new Index during testing
        createKS(keyspace);
        createTableAndIndexForRow();

        assertThat(countResults("sample_table", "part=0 AND uid > 4 AND magic='" + q("searchName", "/.*?AT.*/") + "' ALLOW FILTERING", true), is(4));
        assertThat(countResults("sample_table", "part=0 AND uid < 4 AND magic='" + q("searchName", "/.*?AT.*/") + "'  ALLOW FILTERING", true), is(1));
        assertThat(countResults("sample_table", "magic = '" + q("searchName", "/.*?CT.*/") + "'", true), is(5));
        assertThat(countResults("sample_table", "magic = '" + pfq("searchName", "ca") + "'", true), is(5));
        assertThat(countResults("sample_table", "magic = '" + mq("searchName", "CATV") + "'", true), is(5));
        assertThat(countResults("sample_table", "magic = '" + fq(1, "searchName", "CATA") + "'", true), is(5));
        assertThat(countResults("sample_table", "magic = '" + fq(0, "searchName", "CCTA") + "'", true), is(0));
        assertThat(countResults("sample_table", "magic = '" + fq(1, "searchName", "CZTV") + "'", true), is(10));
        assertThat(countResults("sample_table", "magic = '" + q("searchName", "CATV CCTV") + "'", true), is(10));
        assertThat(countResults("sample_table", "magic = '" + phq(0, "searchName", "aaaa", "BBBB") + "'", true), is(1));
        assertThat(countResults("sample_table", "magic = '" + gtq("searchName", "CATV") + "'", true), is(6));
        assertThat(countResults("sample_table", "magic = '" + gtq("otherid", "9") + "'", true), is(3));
    }

    private void createTableAndIndexForRow() {
        //add idx options with DOCS_AND_FREQS_AND_POSITIONS for phrase queries.
        String options = "{\n" +
                "\t\"fields\":{\n" +
                "\t\t\"searchName\":{\"indexOptions\":\"DOCS_AND_FREQS_AND_POSITIONS\"},\n" +
                "\t\t\"otherName\":{},\n" +
                "\t\t\"otherid\":{}\n" +
                "\t}\n" +
                "}\n";

        getSession().execute("USE " + keyspace + ";");
        getSession().execute("CREATE TABLE sample_table (part int,uid int,otherid int,othername varchar,searchName varchar,magic text,PRIMARY KEY (part, uid,otherid,searchName));");

        getSession().execute("CREATE CUSTOM INDEX sample_table_searchName_key ON sample_table(magic) USING 'com.tuplejump.stargate.RowIndex' WITH options ={'sg_options':'" + options + "'}");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 1,  1, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 2,  2, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 4,  4, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 5,  5, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 8,  8, 'CCTV', 'CCTV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 3,  3, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 6,  6, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 9,  9, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 7,  7, 'CATV', 'CATV')");
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 10, 10, 'CATV', 'CATV')");
        //slop 1
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 11,  11, 'AAAA ', 'AAAA aaaa cccc BBBB')");
        //slop 0
        getSession().execute("INSERT INTO sample_table (part, uid, otherid,othername, searchName) VALUES (0, 12,  12, 'AAAA ', 'AAAA bbbb aaaa AAAA BBBB')");

    }
}