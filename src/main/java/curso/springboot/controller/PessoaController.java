package curso.springboot.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import curso.springboot.model.Pessoa;
import curso.springboot.model.Telefone;
import curso.springboot.repository.PessoaRepository;
import curso.springboot.repository.TelefoneRepository;

@Controller
public class PessoaController {
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@RequestMapping(method=RequestMethod.GET, value="/cadastropessoa")
	public ModelAndView iniciar() {
		
		Iterable<Pessoa> pessoas = pessoaRepository.findAll();
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		
		modelAndView.addObject("pessoaobj", new Pessoa());
		
		modelAndView.addObject("pessoas", pessoas);
		
		return modelAndView;
	}
	
	// O valor **/ antes de salvarpessoa na string "**/salvarpessoa, ignora qualquer coisa na url
	// que vem antes do /salvarpessoa, considerando apenas o /salvar pessoa como um trigger para chamar o controller
	@RequestMapping(method=RequestMethod.POST, value="**/salvarpessoa")
	public ModelAndView salvar(@Valid Pessoa pessoa, BindingResult bindingResult) {
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		
		Iterable<Pessoa> pessoas = pessoaRepository.findAll();
		
		if(bindingResult.hasErrors()) {
			
			modelAndView.addObject("pessoas", pessoas);
			
			modelAndView.addObject("pessoaobj", pessoa);
			
			// Retorna as mensagens de erro para a view
			List<String> mensagensDeErro = new ArrayList<String>();
			
			for(ObjectError objectError : bindingResult.getAllErrors()) {
				
				mensagensDeErro.add(objectError.getDefaultMessage());
			}
			
			modelAndView.addObject("mensagensDeErro", mensagensDeErro);
			
			return modelAndView;
		}
		
		pessoaRepository.save(pessoa);
		
		modelAndView.addObject("pessoas", pessoas);
		
		modelAndView.addObject("pessoaobj", new Pessoa());
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/listapessoas")
	public ModelAndView listar() {
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		
		Iterable<Pessoa> pessoas = pessoaRepository.findAll();
		
		modelAndView.addObject("pessoas", pessoas);
		
		modelAndView.addObject("pessoaobj", new Pessoa());
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/editarpessoa/{idpessoa}")
	public ModelAndView editar(@PathVariable("idpessoa") Long idPessoa) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		
		modelAndView.addObject("pessoaobj", pessoa.get());
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/removerpessoa/{idpessoa}")
	public ModelAndView remover(@PathVariable("idpessoa") Long idPessoa) {
		
		pessoaRepository.deleteById(idPessoa);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		
		modelAndView.addObject("pessoas", pessoaRepository.findAll());
		
		modelAndView.addObject("pessoaobj", new Pessoa());
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.POST, value="**/pesquisarpessoa")
	public ModelAndView pesquisar(@RequestParam("nomepesquisa") String nome) {
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastropessoa");
		
		modelAndView.addObject("pessoas", pessoaRepository.findPessoaByNome(nome));
		
		modelAndView.addObject("pessoaobj", new Pessoa());
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/inserirtelefone/{idpessoa}")
	public ModelAndView inserirTelefones(@PathVariable("idpessoa") Long idPessoa) {
		
		Optional<Pessoa> pessoa = pessoaRepository.findById(idPessoa);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");
		
		modelAndView.addObject("pessoaobj", pessoa.get());
		
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.POST, value="**/salvarTelefone/{idpessoa}")
	public ModelAndView salvarTelefone(Telefone telefone, @PathVariable("idpessoa") Long idPessoa) {
		
		Pessoa pessoa = pessoaRepository.findById(idPessoa).get();
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");
		
		modelAndView.addObject("pessoaobj", pessoa);
		
		Boolean telefoneValido = telefone != null && 
								 (telefone.getNumero() != null && telefone.getNumero().isEmpty()) ||
								 telefone.getNumero() == null;
		
		if(!telefoneValido) {
			
			modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));
			
			return modelAndView;
		}
		
		telefone.setPessoa(pessoa);
		
		telefoneRepository.save(telefone);
		
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(idPessoa));
		
		return modelAndView;
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/removerTelefone/{idtelefone}")
	public ModelAndView excluirTelefone(@PathVariable("idtelefone") Long idTelefone) {
		
		Pessoa pessoa = telefoneRepository.findById(idTelefone).get().getPessoa();
		
		telefoneRepository.deleteById(idTelefone);
		
		ModelAndView modelAndView = new ModelAndView("cadastro/cadastrotelefone");
		
		modelAndView.addObject("pessoaobj", pessoa);
		
		modelAndView.addObject("telefones", telefoneRepository.getTelefones(pessoa.getId()));
		
		return modelAndView;
	}
}
