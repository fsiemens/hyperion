package de.fabiansiemens.hyperion.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import de.fabiansiemens.hyperion.core.util.asciitable.FormatService;
import de.fabiansiemens.hyperion.core.util.asciitable.DataType;
import de.fabiansiemens.hyperion.core.util.asciitable.TableColumn;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class Test_FormatService {

	private FormatService formatService;
	
	@Autowired
	public Test_FormatService(FormatService tableService) {
		this.formatService = tableService;
	}

	@Test
	public void testAsciiTable() {
		TableColumn a = new TableColumn("ColumnA", DataType.STRING, 30, true);
		TableColumn b = new TableColumn("ColumnB", DataType.NUMERIC, 5, false);
		TableColumn c = new TableColumn("ColumnC", DataType.STRING, 10, false);
		TableColumn d = new TableColumn("ColumnD", DataType.STRING, 15, true);
		
		TableColumn[] headers = {a, b, c, d};
		String[][] rows = {
				{"Test", "150", "Much tooo long Text", "Test2"},
				{"Row2 Muuuuch mucch text so much text omg", "8900000", "Abcd", "Short"}
		};
		
		String table = formatService.formatTable(headers, rows, 50);
		log.info("Output table: \n" + table);
		assertThat(table).isEqualTo("ColumnA                |Col..|ColumnC   |ColumnD \n"
				+ "-----------------------+-----+----------+--------\n"
				+ "Test                   |150  |Much too..|Test2   \n"
				+ "Row2 Muuuuch mucch te..|~8,9M|Abcd      |Short   \n");
	}
	
	@Test
	public void testFormat() {
        assertThat(formatService.formatNumber(1560000, 5)).isEqualTo("~1,6M"); // ~1.6M
        assertThat(formatService.formatNumber(1560000, 3)).isEqualTo("~2M"); // ~2M
        assertThat(formatService.formatNumber(999, 3)).isEqualTo("999");     // 999
        assertThat(formatService.formatNumber(123456789, 4)).isEqualTo("~123M"); // ~123M
        assertThat(formatService.formatNumber(987654321, 5)).isEqualTo("~988M"); // ~988M
	}
}
