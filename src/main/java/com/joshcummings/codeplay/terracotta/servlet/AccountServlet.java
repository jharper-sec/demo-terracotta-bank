/*
 * Copyright 2015-2018 Josh Cummings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.joshcummings.codeplay.terracotta.servlet;

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Josh Cummings
 */
//@WebServlet("/showAccounts")
public class AccountServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private SpelExpressionParser parser = new SpelExpressionParser();
	private AccountService accountService;

	public AccountServlet(AccountService accountService) {
		this.accountService = accountService;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ( request.getAttribute("authenticatedUser") == null ) {
			response.sendRedirect(request.getContextPath() + "/employee.jsp?relay=" + request.getRequestURL().toString());
		} else {
			Set<Account> accounts = this.accountService.findAll();
			if (StringUtils.hasText(request.getParameter("showAccountsSearch"))) {
				String filter = request.getParameter("showAccountsSearch");
				Expression expression = this.parser.parseExpression("#this.?[" + filter + "]");
				Object value = expression.getValue(new StandardEvaluationContext(accounts));
				if (value instanceof List<?>) {
					@SuppressWarnings("unchecked")
					List<Account> filteredAccounts = (List<Account>) value;
					accounts = new LinkedHashSet<>(filteredAccounts);
				}
			}
			request.setAttribute("accounts", accounts);
			request.getRequestDispatcher("/WEB-INF/accounts.jsp").forward(request, response);
		}
	}
}
