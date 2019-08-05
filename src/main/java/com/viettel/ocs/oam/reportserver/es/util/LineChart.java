package com.viettel.ocs.oam.reportserver.es.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.PresetColor;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFLineProperties;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.AxisCrosses;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.LegendPosition;
import org.apache.poi.xddf.usermodel.chart.MarkerStyle;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFChartLegend;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFLineChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * Line chart example.
 */
public class LineChart {
	private LineChart() {

	}

	public static void main(String[] args) throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			XSSFSheet sheet = wb.createSheet("linechart");
			final int NUM_OF_ROWS = 3;
			final int NUM_OF_COLUMNS = 10;

			// Create a row and put some cells in it. Rows are 0 based.
			Row row;
			Cell cell;
			for (int rowIndex = 0; rowIndex < NUM_OF_ROWS; rowIndex++) {
				row = sheet.createRow((short) rowIndex);
				for (int colIndex = 0; colIndex < NUM_OF_COLUMNS; colIndex++) {
					cell = row.createCell((short) colIndex);
					cell.setCellValue(colIndex * (rowIndex + 1.0));
				}
			}

			XSSFDrawing drawing = sheet.createDrawingPatriarch();
			XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 5, 10, 15);

			XSSFChart chart = drawing.createChart(anchor);
			XDDFChartLegend legend = chart.getOrAddLegend();
			legend.setPosition(LegendPosition.TOP_RIGHT);

			// Use a category axis for the bottom axis.
			XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
			bottomAxis.setTitle("Time"); // https://stackoverflow.com/questions/32010765
			XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
			leftAxis.setTitle("%");
			leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

			XDDFDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
					new CellRangeAddress(0, 0, 0, NUM_OF_COLUMNS - 1));
			XDDFNumericalDataSource<Double> ys1 = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
					new CellRangeAddress(1, 1, 0, NUM_OF_COLUMNS - 1));
			XDDFNumericalDataSource<Double> ys2 = XDDFDataSourcesFactory.fromNumericCellRange(sheet,
					new CellRangeAddress(2, 2, 0, NUM_OF_COLUMNS - 1));

			XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
			XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) data.addSeries(xs, ys1);
			series1.setTitle("average CPU percent", null); // https://stackoverflow.com/questions/21855842
			series1.setSmooth(false); // https://stackoverflow.com/questions/29014848
			series1.setMarkerStyle(MarkerStyle.STAR); // https://stackoverflow.com/questions/39636138
			XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(xs, ys2);
			series2.setTitle("average RAM percent", null);
			series2.setSmooth(true);
			series2.setMarkerSize((short) 6);
			series2.setMarkerStyle(MarkerStyle.TRIANGLE); // https://stackoverflow.com/questions/39636138
			chart.plot(data);

			// if your series have missing values like
			// https://stackoverflow.com/questions/29014848
			// chart.displayBlanksAs(DisplayBlanks.GAP);

			// https://stackoverflow.com/questions/24676460
			solidLineSeries(data, 0, PresetColor.CHARTREUSE);
			solidLineSeries(data, 1, PresetColor.TURQUOISE);

			// Write the output to a file
			try (FileOutputStream fileOut = new FileOutputStream("src/main/resources/GeneratedReport.xlsx")) {
				wb.write(fileOut);
			}
		}
	}

	private static void solidLineSeries(XDDFChartData data, int index, PresetColor color) {
		XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(color));
		XDDFLineProperties line = new XDDFLineProperties();
		line.setFillProperties(fill);
		XDDFChartData.Series series = data.getSeries().get(index);
		XDDFShapeProperties properties = series.getShapeProperties();
		if (properties == null) {
			properties = new XDDFShapeProperties();
		}
		properties.setLineProperties(line);
		series.setShapeProperties(properties);
	}

	public static void drawChartToImage(Map<String, Map<String, Double>> fieldMap, List<String> groups,
			String outputPath) throws IOException, ParseException {

		String title = "";
		for (String group : groups)
			title += group + " ";

		TimeSeriesCollection dataset = new TimeSeriesCollection();
		for (String field : fieldMap.keySet()) {
			TimeSeries series = new TimeSeries(field);
			Map<String, Double> timeMap = fieldMap.get(field);
			for (String time : timeMap.keySet()) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = sdf.parse(time);
				series.add(new Second(date), timeMap.get(time));
			}
			dataset.addSeries(series);
		}

		JFreeChart chart = ChartFactory.createXYLineChart(
				title, "Time", "Percent", dataset, PlotOrientation.VERTICAL, true, false, false);

		XYPlot xyplot = (XYPlot) chart.getXYPlot();
		xyplot.setDomainAxis(new DateAxis());
		DateAxis axis = (DateAxis) xyplot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
		NumberAxis range = (NumberAxis) xyplot.getRangeAxis();
		range.setRange(0.0, 100.0);

		OutputStream out = new FileOutputStream(outputPath);
		ChartUtilities.writeChartAsPNG(out, chart, 1500, 600);
	}

	public static void importImagesToExcel(List<String> imagePaths, String outputPath) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Charts");

		for (int imgIdx = 0; imgIdx < imagePaths.size(); imgIdx++) {
			String path = imagePaths.get(imgIdx);
			InputStream image = new FileInputStream(path);
			byte[] bytes = IOUtils.toByteArray(image);
			int imageId = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
			image.close();

			HSSFPatriarch drawing = sheet.createDrawingPatriarch();

			ClientAnchor anchor = new HSSFClientAnchor();

			HSSFPicture picture = drawing.createPicture(anchor, imageId);

			anchor.setCol1(1);
			anchor.setRow1(1 + 30 * imgIdx);

			picture.resize();
			
			double imgHeight = picture.getImageDimension().getHeight();
			double cellHeight = sheet.createRow(0).getHeightInPoints();
			picture.resize(30 * cellHeight / imgHeight);
		}

		FileOutputStream out = new FileOutputStream(outputPath);
		workbook.write(out);
		workbook.close();
		out.close();
	}
}