package curso.springboot.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.ProfissaoRepository;
import curso.springboot.repository.TelefoneRepository;
import curso.springboot.util.ReportUtil;

@Controller
public class PessoaController {

	@Autowired
	private PessoaRepository pessoaRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ProfissaoRepository profissaoRepository;

	@Autowired
	private ReportUtil reportUtil;

	@RequestMapping(method = RequestMethod.GET, value = "/cadastropessoa")
	public ModelAndView iniciar() {

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");

		modelAndView.addObject("pessoaobj", new Pessoa());

		/* Implementação de paginacao via sobrecarga do metodo findAll */
		
		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));
		
		modelAndView.addObject("profissoes", profissaoRepository.findAll());

		return modelAndView;
	}
	
	/* Metodo chamado ao selecionar um numero de pagina da paginação presente no front-end */
	
	@RequestMapping(method = RequestMethod.GET, value = "/pessoasporpaginacao")
	public ModelAndView carregarPessoasPorPaginacao(@PageableDefault(size = 5) Pageable pageable, ModelAndView modelAndView) {
		
		Page<Pessoa> pagePessoa = pessoaRepository.findAll(pageable);
		
		modelAndView.addObject("pessoas", pagePessoa);
		
		modelAndView.addObject("pessoaobj", new Pessoa());
		
		modelAndView.setViewName("cadastro/cadastropessoa");
		
		return modelAndView;
	}

	// O valor **/ antes de salvarpessoa na string "**/salvarpessoa, ignora qualquer
	// coisa na url
	// que vem antes do /salvarpessoa, considerando apenas o /salvar pessoa como um
	// trigger para chamar o controller
	@RequestMapping(method = RequestMethod.POST, 
			        value = "**/salvarpessoa",
			        consumes = {"multipart/form-data"})
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult, final MultipartFile file) throws IOException {
		
		System.out.println(file.getContentType());
		
		System.out.println(file.getOriginalFilename());
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");

		if (bindingResult.hasErrors()) {

			modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));

			modelAndView.addObject("pessoaobj", pessoa);

			// Retorna as mensagens de erro para a view
			List<String> mensagensDeErro = new ArrayList<String>();

			for (ObjectError objectError : bindingResult.getAllErrors()) {

				mensagensDeErro.add(objectError.getDefaultMessage());
			}

			modelAndView.addObject("mensagensDeErro", mensagensDeErro);
			
			modelAndView.addObject("profissoes", profissaoRepository.findAll());

			return modelAndView;
		}

		// Comando para corrigir erro ao editar endereço de uma pessoa ja cadastrada e
		// que possui telefones
		pessoa.setTelefones(telefoneRepository.getTelefones(pessoa.getId()));
		
		if(file.getSize() > 0) {
			
			pessoa.setArquivo(file.getBytes());
			
			pessoa.setNomeDoArquivo(file.getOriginalFilename());
			
			pessoa.setTipoDoArquivo(file.getContentType());
		}
		else if(pessoa.getId() != null && pessoa.getId() > 0){
			
			Pessoa variavelAuxiliar = pessoaRepository.findById(pessoa.getId()).get();
			
			pessoa.setArquivo(variavelAuxiliar.getArquivo());
			
			pessoa.setNomeDoArquivo(variavelAuxiliar.getNomeDoArquivo());
			
			pessoa.setTipoDoArquivo(variavelAuxiliar.getTipoDoArquivo());
		}
		
		pessoaRepository.save(pessoa);

		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));

		modelAndView.addObject("pessoaobj", new Pessoa());
		
		modelAndView.addObject("profissoes", profissaoRepository.findAll());

		return modelAndView;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/listapessoas")
	public ModelAndView listar() {

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");

		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));

		modelAndView.addObject("pessoaobj", new Pessoa());
		
		modelAndView.addObject("profissoes", profissaoRepository.findAll());

		return modelAndView;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idPessoa) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");

		modelAndView.addObject("pessoaobj", pessoa.get());
		
		modelAndView.addObject("profissoes", profissaoRepository.findAll());

		return modelAndView;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/removerpessoa/{idpessoa}")
	public ModelAndView remover(@PathVariable("idpessoa") Long idPessoa) {

		pessoaRepository.deleteById(idPessoa);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");

		modelAndView.addObject("pessoas", pessoaRepository.findAll(PageRequest.of(0, 5, Sort.by("nome"))));

		modelAndView.addObject("pessoaobj", new Pessoa());
		
		modelAndView.addObject("profissoes", profissaoRepository.findAll());

		return modelAndView;
	}
	
	
	@RequestMapping(method = RequestMethod.GET, value ="**/downloadarquivo/{idpessoa}")
	public void downloadArquivoPessoal(@PathVariable("idpessoa") Long idPessoa,
									   HttpServletResponse response) throws IOException {
		
		/* Consulta o objeto pessoa no banco de dados */
		Pessoa pessoa = pessoaRepository.findById(idPessoa).get();
		
		if(pessoa.getArquivo() != null) {
			
			/* Informa o tamanha do resposta */
			response.setContentLength(pessoa.getArquivo().length);
			
			/* Informa o tipo de arquivo que sera disponibilizado para download */
			response.setContentType(pessoa.getTipoDoArquivo());
			
			/* Define o cabeçalho da resposta */
			String headerKey = "Content-Disposition";
			
			String headerValue = String.format("attachment; filename=\"%s\"", pessoa.getNomeDoArquivo());
			
			response.setHeader(headerKey, headerValue);
			
			/* Finaliza a resposta passando o arquivo */
			response.getOutputStream().write(pessoa.getArquivo());
		}
	}
	

	@RequestMapping(method = RequestMethod.POST, value = "**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("pesquisanome") String nome,
								  @RequestParam("pesquisasexo") String sexo,
		                          @PageableDefault(size = 5, sort = {"nome"}) Pageable pageable) {

		Page<Pessoa> pessoas = null;

		if (sexo != null && !sexo.isEmpty()) {

			//pessoas = pessoaRepository.findPessoaByNomeESexo(nome, sexo);
		} else {

			pessoas = pessoaRepository.findPessoaByNome(nome, pageable);
		}

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");

		modelAndView.addObject("pessoas", pessoas);

		modelAndView.addObject("pessoaobj", new Pessoa());
		
		modelAndView.addObject("pesquisanome", nome);

		return modelAndView;
	}

	@RequestMapping(method = RequestMethod.GET, value = "**/pesquisarpessoa")
	public void gerarRelatorioDeClientes(@RequestParam("pesquisanome") String nome,
			@RequestParam("pesquisasexo") String sexo, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		List<Pessoa> pessoas = new ArrayList<Pessoa>();

		if (sexo != null && !sexo.isEmpty() && nome != null && !nome.isEmpty()) {

			pessoas = pessoaRepository.findPessoaByNomeESexo(nome, sexo);
		} else if (nome != null && !nome.isEmpty()) {

			pessoas = pessoaRepository.findPessoaByNome(nome);
		} else if (sexo != null && !sexo.isEmpty()) {

			pessoas = pessoaRepository.findPessoaBySexo(sexo);
		} else {

			Iterable<Pessoa> iterator = pessoaRepository.findAll();

			for (Pessoa pessoa : iterator) {

				pessoas.add(pessoa);
			}
		}

		/* Chama o serviço que realiza a geração do relatorio */
		byte[] relatorioEmPdf = reportUtil.gerarRelatorio(pessoas, "pessoa", request.getServletContext());

		/* Tamanho da resposta */
		response.setContentLength(relatorioEmPdf.length);

		/* Definir na resposta o tipo de arquivo */
		response.setContentType("application/octet-stream");

		/* Definir o cabeçalho da resposta */
		String headerKey = "Content-Disposition";

		String headerValue = String.format("attachment; filename=\"%s\"", "relatorio-de-clientes.pdf");

		response.setHeader(headerKey, headerValue);

		/* Finaliza e retorna o conteudo para o navegador */
		response.getOutputStream().write(relatorioEmPdf);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/inserirtelefone/{idpessoa}")
	public ModelAndView inserirTelefones(@PathVariable("idpessoa") Long idPessoa) {

		Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");

		modelAndView.addObject("pessoaobj", pessoa.get());

		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));

		return modelAndView;
	}

	@RequestMapping(method = RequestMethod.POST, value = "**/salvartelefone/{idpessoa}")
	public ModelAndView salvarTelefone(Telefone telefone, @PathVariable("idpessoa") Long idPessoa) {

		Pessoa pessoa = pessoaRepository.findById(idPessoa).get();

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");

		modelAndView.addObject("pessoaobj", pessoa);

		Boolean telefoneInvalido = (telefone != null && (telefone.getNumero() != null && telefone.getNumero().isEmpty())
				|| telefone.getNumero() == null || (telefone.getTipo() != null && telefone.getTipo().isEmpty())
				|| telefone.getTipo() == null);

		if (telefoneInvalido) {

			modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));

			List<String> mensagensDeErro = new ArrayList<String>();

			if (telefone.getNumero().isEmpty()) {
				mensagensDeErro.add("O numero deve ser informado!");
			}

			if (telefone.getTipo().isEmpty()) {
				mensagensDeErro.add("O tipo deve ser informado!");
			}

			modelAndView.addObject("mensagensDeErro", mensagensDeErro);

			return modelAndView;
		}

		telefone.setPessoa(pessoa);

		telefoneRepository.save(telefone);

		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));

		return modelAndView;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/removertelefone/{idtelefone}")
	public ModelAndView excluirTelefone(@PathVariable("idtelefone") Long idTelefone) {

		Pessoa pessoa = telefoneRepository.findById(idTelefone).get().getPessoa();

		telefoneRepository.deleteById(idTelefone);

		ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");

		modelAndView.addObject("pessoaobj", pessoa);

		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));

		return modelAndView;
	}
}
