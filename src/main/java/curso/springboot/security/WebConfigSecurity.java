package curso.springboot.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {
	
	@Override // Configura as solicitacoes de acesso por Http
	protected void configure(HttpSecurity http) throws Exception {
		
		http.csrf()
		.disable() // Desativa as configuracoes padrao de memoria
		.authorizeRequests() // Permite restringir acessos
		.antMatchers(HttpMethod.GET, "/").permitAll() // Permite qualquer usuario acessar a pagina inicial
		.anyRequest().authenticated()
		.and().formLogin().permitAll() // Permite qualquer usuario acessar o formulario de login
		.and().logout() // Mapeia URL de logout e invalida usuario que estav autenticado
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
	}
	
	@Override // Cria autenticacao do usuario com banco de dados ou em memoria
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
		.withUser("igor")
		.password("$2a$10$.IYH.Q6bY9dHbuKaOXGom.2UCz8M.hUyUNV9PwuHdT.7K3EMyz5ru")
		.roles("ADMIN");
	}
	
	@Override // Ignora URL's especificas
	public void configure(WebSecurity web) throws Exception {
		
		web.ignoring().antMatchers("/materialize/**");
	}
}
