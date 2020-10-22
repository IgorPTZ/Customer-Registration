package curso.springboot.util;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.stereotype.Component;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Component
public class ReportUtil implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/* Armazena o PDF gerado em bytes para download */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public byte[] gerarRelatorio(List listaDeDados, 
			                     String relatorio, 
			                     ServletContext servletContext) throws Exception {
		
		/* Cria a lista de dados para o relatorio a partir do recebimento de uma lista de objetos */
		JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listaDeDados);
		
		/* Carrega o caminho do arquivo Jasper compilado */
		String caminhoDoArquivoJasper = servletContext.getRealPath("relatorios")
				                        + File.separator + relatorio + ".jasper";
		
		/* Carrega o arquivo Jasper passando os dados */
		JasperPrint jasperPrinter = JasperFillManager.fillReport(caminhoDoArquivoJasper, 
				                                                 new HashMap(),
				                                                 dataSource);
		
		return JasperExportManager.exportReportToPdf(jasperPrinter);
	}
}
