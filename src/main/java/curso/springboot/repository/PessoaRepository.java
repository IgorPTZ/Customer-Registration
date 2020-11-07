package curso.springboot.repository;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.springboot.model.Pessoa;

@Repository
@Transactional
public interface PessoaRepository extends JpaRepository<Pessoa, Long>{
	
	@Query("select p from Pessoa p where p.nome like %?1%")
	List<Pessoa> findPessoaByNome(String nome);
	
	@Query("select p from Pessoa p where p.sexo like %?1%")
	List<Pessoa> findPessoaBySexo(String sexo);
	
	@Query("select p from Pessoa p where p.nome like %?1% and p.sexo = ?2")
	List<Pessoa> findPessoaByNomeESexo(String nome, String sexo);
	
	default Page<Pessoa> findPessoaByNome(String nome, Pageable pageable) {
		
		Pessoa pessoa = new Pessoa();
		
		pessoa.setNome(nome);
		
		/* Configurando a pesquisa para pesquisar pelo nome (Semelhante ao LIKE do SQL) */
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny()
										.withMatcher("nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		
		
		/* Une o objeto com o valor e a configuração para pesquisar */
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);
		
		Page<Pessoa> pessoas = findAll(example, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("nome" )));
				
		return pessoas;
	}
	
	default Page<Pessoa> findPessoaByNomeESexo(String nome, String sexo, Pageable pageable) {
		
		Pessoa pessoa = new Pessoa();
		
		pessoa.setNome(nome);
		
		pessoa.setSexo(sexo);
		
		ExampleMatcher exampleMatcher = ExampleMatcher.matching()
										.withMatcher("nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
										.withMatcher("sexo", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		
		Example<Pessoa> example = Example.of(pessoa, exampleMatcher);
		
		Page<Pessoa> pessoas = findAll(example, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("nome")));
		
		return pessoas;
	}
}
