package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.ApplicationContextLoad;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {
	
	
	/*Tem de validade do Token 2 dias*/
	private static final long EXPIRATION_TIME = 172800000;
	
	/*Uma senha unica para compor a autenticacao e ajudar na seguranca*/
	private static final String SECRET = "SenhaExtremamenteSecreta";
	
	/*Prefixo padrao de Token*/
	private static final String TOKEN_PREFIX = "Bearer";
	
	private static final String HEADER_STRING = "Authorization";
	
	/*Gerando token de autenticado e adiconando ao cabecalho e resposta Http*/
	public void addAuthentication(HttpServletResponse response , String username) throws IOException {
		
		/*Montagem do Token*/
		String JWT = Jwts.builder() /*Chama o gerador de Token*/
				        .setSubject(username) /*Adicona o usuario*/
				        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) /*Tempo de expiracao*/
				        .signWith(SignatureAlgorithm.HS512, SECRET).compact(); /*Compactacao e algoritmos de geracao de senha*/
		
		/*Junta token com o prefixo*/
		String token = TOKEN_PREFIX + " " + JWT; /*Bearer ---> numeros aleatorios*/
		
		/*Adiciona no cabecalho http*/
		response.addHeader(HEADER_STRING, token); /*Authorization: Bearer ----> numeros aleatorios*/
		
		liberacaoCors(response);
		
		/*Escreve token como responsta no corpo http*/
		response.getWriter().write("{\"Authorization\": \""+token+"\"}");
		
	}
	
	
	/*Retorna o usuario validado com token ou caso nao seja valido retorna null*/
	public Authentication getAuhentication(HttpServletRequest request, HttpServletResponse response) {
		
		/*Pega o token enviado no cabecalho http*/
		
		String token = request.getHeader(HEADER_STRING);
		
		if (token != null) {
			
			/*Faz a validacao do token do usuario na requisicao*/
			String user = Jwts.parser().setSigningKey(SECRET) /*Bearer --> numero de token*/
								.parseClaimsJws(token.replace(TOKEN_PREFIX, "")) /*numero aleatorio*/
								.getBody().getSubject(); /*Nome aleatorio*/
			if (user != null) {
				
				Usuario usuario = ApplicationContextLoad.getApplicationContext()
						        .getBean(UsuarioRepository.class).findUserByLogin(user);
				
				if (usuario != null) {
					
					return new UsernamePasswordAuthenticationToken(
							usuario.getLogin(), 
							usuario.getSenha(),
							usuario.getAuthorities());
					
				}
			}
			
		}
		
		liberacaoCors(response);
		return null; /*Nao autorizado*/
		
	}


	private void liberacaoCors(HttpServletResponse response) {
		if(response.getHeader("Access-Control-Allow-Origin")== null) {
			response.addHeader("Access-Control-Allow-Origin", "*");
		}
		
		if(response.getHeader("Access-Control-Allow-Headers")== null) {
			response.addHeader("Access-Control-Allow-Headers", "*");
		}
		
		if(response.getHeader("Access-Control-Request-Headers")== null) {
			response.addHeader("Access-Control-Request-Headers", "*");
		}
		
		if(response.getHeader("Access-Control-Allow-Methods")== null) {
			response.addHeader("Access-Control-Allow-Methods", "*");
		}
		
	}

}