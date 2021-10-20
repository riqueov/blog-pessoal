package org.generation.blogPessoal.service;

import java.nio.charset.Charset;
import java.util.Optional;
import org.apache.commons.codec.binary.Base64;

import org.generation.blogPessoal.repository.UsuarioRepository;
import org.generation.blogPessoal.model.UsuarioLogin;
import org.generation.blogPessoal.model.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
	
	@Autowired
	private UsuarioRepository repository;
	
	
	private static String criptografarSenha(String senha) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		return encoder.encode(senha);
	}

	public Optional<Object> cadastrarUsuario(Usuario usuario) {
		return repository.findByUsuario(usuario.getUsuario()).map(resp -> {
			return Optional.empty();
		}).orElseGet(() -> {
			usuario.setSenha(criptografarSenha(usuario.getSenha()));
			return Optional.ofNullable(repository.save(usuario));
		});
	}

	public Optional<UsuarioLogin> logar(Optional<UsuarioLogin> user) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		Optional<Usuario> usuario = repository.findByUsuario(user.get().getUsuario());

		if (usuario.isPresent()) {
			if (encoder.matches(user.get().getSenha(), usuario.get().getSenha())) {

				String auth = user.get().getUsuario() + ":" + user.get().getSenha();
				byte[] encodeAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodeAuth);

				user.get().setId(usuario.get().getId());
				user.get().setToken(authHeader);
				user.get().setNome(usuario.get().getNome());
				user.get().setSenha(usuario.get().getSenha());

				return user;
			}
		}
		return null;
	}

	public Optional<Usuario> atualizarUsuario(Usuario usuario) {
		return repository.findById(usuario.getId()).map(resp -> {
			resp.setNome(usuario.getNome());
			resp.setSenha(criptografarSenha(usuario.getSenha()));
			return Optional.ofNullable(repository.save(resp));

		}).orElseGet(() -> {
			return Optional.empty();
		});
	}
}
