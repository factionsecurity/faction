package com.fuse.actions.unittests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.python.icu.util.Calendar;

import com.fuse.dao.Assessment;
import com.fuse.dao.ExploitStep;
import com.fuse.dao.FinalReport;
import com.fuse.dao.HibHelper;
import com.fuse.dao.Permissions;
import com.fuse.dao.Teams;
import com.fuse.dao.User;
import com.fuse.dao.Vulnerability;
import com.fuse.utils.AccessControl;

public class LoadTester2 {
	private static String editorData="<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAWYAAAC&#43;CAYAAAAGPKXgAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAACFZSURBVHhe7Z17lBTVncdbjSQxx83JbmJM0KjIMT7WP3I0i5ogRJAoLpqsEXf1iMdVjDE&#43;o8SNbqJnc3ajxoR4QBkY3hIIwag8Z4YBGZgB5gEzzPB0RoTAwMAMzxlGHoLfvbdnaryWVdW3uqu6b3V9&#43;5zf6epbt373d7/3V5&#43;6fburOwE&#43;qAAVoAJUIKcKdHR0oLOzE8ePH8fJkyeRyGk0bJwKUAEqQAVQX1&#43;PpqYm7N69G&#43;3t7QQzc4IKUAEqkGsFZs2ahZKSEtTV1aGlpYVgzvWAsH0qQAWowHPPPYdx48ahuLg4OXPmUgZzggpQASqQYwXuvPNOPPvss5gxY0ZyWYNgzvGAsHkqQAWowIABA/DQQw8lZ801NTUEM1OCClABKpBrBfr164f77rsPY8aMQWVlJcGc6wFh&#43;1SAClCBa6&#43;9Fvfffz/Gjh1LMDMdqAAVoAImKDBw4MDkUkZBQQGqq6s5YzZhUBgDFaAC8VZg8ODBeOSRRzBhwgSsWbOGYI53OrD3VIAKmKDAkCFD8Oijj6KwsJBgNmFAGAMVoAJUgGBmDlABKkAFDFOAYDZsQBgOFaACVIBg7s6Bq6&#43;&#43;GjRqYEoOnHfeeTj33HNJqJgqEHswyxPxhhtuSP5gyM6dO2OaBuy2aQrIn3ps2rsPvQcNxVfPOce08BhPyArEGswSyqNHj0ZzczMKFqyjUQPjcqDh0FHM3rSdcA4ZhKa5zwjMiUQCTqZ2Uu439SFnyhLK4xfU06iBsTmw/EAnzvvJ3aaeRowrBAXSArMFY6d4UoE6hD6k5VLOluXSxYRFDTRqYHwO/HWvgLNYd&#43;YjHgr4BrMXlO0zZd26uZBagvno0aMoLFpPowauOTB15hLc038YBn21T9ZMtifbVXNzZsuR5IfTXg&#43;T353m4hyPcpu&#43;wKzOhlN12k/dVL7C2G8l&#43;aTiDaBRA7ccuO2S72LxhEIcWb8maybbk&#43;2qMU3b7Q1mkydBYZy/&#43;e4zNDBL4UxOFgvMk0s2gEYN3HJAzpQ76quxd/bErJlsT7arxjRxF8Gc7zBW&#43;5c2mHXfNunWy7boFphnvPseaNTALQckINtrK9Eyczz2zOo2ud1tVrl8Tm531&#43;vZVurumTnhEz9iO&#43;lPOcbalu3JdtWYfrfdHcymvzvN9rmdD&#43;1lBGYd6OrUyYWQFpj/vKwRTibjdtsny1Pt9zo2m/uskzabbabblomaSkAeqqnA7umvfdreEK&#43;ldZePe/a/cfvwB3tMvpb73Mo/46/bzy7xLNuT7ao6/vYDZzDbP2zPxbnENoNXIGMwZ3q19vstDrdEdLsAuNW3wDyr7H04mXWc331u/nJRLvuQi3bTbdPEeCUgD1YuR/PkP6F5UpftVLatMgll9SFfv/7Mr5KgtpfvlD66fVnHJ/13&#43;5XtyXZVHX&#43;99dNgdvuqaqbnY/CIocd0FPAFZnXd2CsxdAJxAmaqq7/TmrVXInrVt8D8l&#43;Vb4WTWsX73ufnLRbnsQy7aTbdNE&#43;OVgNxfsRQ7Cn6Pv4/vtoJXlO2ustdGPZOE8KlTp3rM6bWst6Pbj/Xc5feVnnLZnmxX1fFX738CZh0om/pOVYcNrAOEAuZUH/p5AdgNtOkC2ClBZZkF5tnlW&#43;Fkso4st56tOl7l9rrW8U4&#43;1DK1b/ZY7Fqp&#43;619bvHb/TrF7ta2WtcpVi/N7Fo5vXaK3a1NdRzcjnPTIl39rJgtMG977XfYNlaYeN4ut7vN2t4u9o19elQSzidOnPiMyXK5v&#43;d4UV/10&#43;NPlFtgVjV&#43;qqnjM1&#43;XcwI0oZYfCvgGs9XtTK7aumBWoapzjDokqepbYJ5TsQ1OJo&#43;X5dazVcepXK3jtm0/PlW7Xm3b99ljdGvL0sRrv7rP3hc/ffPSyUuvdPapWnrF6GecLJ8SzO9N&#43;COKB/2zlv3PsFuScN67d2&#43;PydeyXNeHbE&#43;2q/brsUaCOT&#43;Qq9eLtMGsu6yhwtXtGB2gppod&#43;G3HAvObK7fDyaQ/q9zadiqTdeyxqf7UY&#43;z&#43;7PUsP2717O2r7abqgxWnV2xu/dM51k0rN&#43;2cYrdr5TeeoPWTfbLAXHrzVVg89CqUCrOe5Xbp0KtRerMwsf3bH9&#43;ahPKOHTs&#43;Y7Jc7k/Wlb66LXl80ofw1W0WmNWx&#43;tl7BLMe0vKjVkZg9jN79qqbLpitE9FpKJxArta3wPzW6r/DyWRdq9zadiqTddTyVL7sPu3Hu7XhVU&#43;nD05x2uPWbdurz3atUmmnxp5uPLrj4tU/Nw0lmBsnvYplt1/vYv2T5f97x&#43;1JKL///vs95vRa1nP31dWGbE&#43;2q8b0wGaCOT&#43;Qq9cLX2C2z0r9ANEPxFWoekHbrZ5OWxaY36ncASeTvtVyr9f2fXZ/TvvVMt1tt3pB9EH68PKfSg8rBque27O9nXQ11tVMjcuvfvJYCcitbxSgYsRQlI&#43;4JWkV9w5NvlZNQnjz5s09Jl//7q7/SMLaXl6h&#43;pC&#43;kv6E326fsj3ZrqrNvRsJZj2k5UetwMGcarnCaSbrR8pUM2G7L7f6FpjnVjXDyeRxarnOa6st6zj7a1nuVKaWO7Vjlbnts5er7Xv1IVW7fvrvt646Lna9vHTLpn4yLgnI7XOmourhO4X9u2LytVV2J16&#43;995PfY9Zvpb1ncs/Oa7Lh7CffeJftifbVTW9m2D2g4nI1/UNZp1ZsxOcdZcydBV1A67b8fb6Fpjn1exCVEz2ISqxBh1nEH1Px4cEZPOCOagd9UDINrLHv2xPtqtqOHxDO7&#43;VoQuHPKgXGpjtcM4UzE4XBK/Zd6r6Fpjnr9mNqJjsU1RiDTrOIPqejg8JyD1LF6Lh&#43;cfR8JvHsV48rxfPcrvHnn&#43;sa3&#43;yzmOijvVaPgsTZV37u7fla1lP8aFuy/Zku6qG/7Y&#43;MzCr50oecCvvu5AWmP3OmnWWF9zWq&#43;1AT7WurVvfAvOCtbthqtkvPKbGmY24pBZ&#43;2wlCvx9ddg2WiF9727eqLGsm25Ptqv29tSF9MGe6fJj3FDSwg2mDORWcva7QOssQdv/WMUGDeWFtC2jUwC0HCqcsTP4EZzZ/j1m2J9tVYxpa7&#43;/DP/X8IZgNJG&#43;KkDICsxssdd42ucFZx6cOtO3&#43;7d/usGbMRXV7QKMGpufAD9d9dsbs9kG72/mhc15GD2H5GbFvMOssS6SaTXutN7sd63cGkKq&#43;BebidXtBowam58DgOmcwe33Qbp&#43;MeL3rzE&#43;8RbdXvsAc3W5&#43;NnILzCX1raBRA9Nz4AYPMOfTecm&#43;dCkQWzBbM2p5QvJBBUxWQObogNr25Hfg&#43;YiHAgQzwRyPTI9wLwnmCA9emqETzARzmqnDw7KlAMGcLaXNaYdgJpjNyUZG4qgAwRy/xCCYCeb4ZX3EekwwR2zAAgiXYCaYA0gjughTAYI5THXN9E0wE8xmZiaj6lGAYI5fMhDMBHP8sj5iPSaYIzZgAYRLMBPMAaQRXYSpAMEcprpm&#43;iaYCWYzM5NRcSkjxjlAMBPMMU7/aHSdM&#43;ZojFOQURLMBHOQ&#43;URfIShAMIcgquEuCWaC2fAUZXgEc/xygGAmmOOX9RHrMcEcsQELIFyCmWAOII3oIkwFCOYw1TXTN8FMMJuZmYyK38qIcQ4QzARzjNM/Gl3njDka4xRklAQzwRxkPtFXCAoQzCGIarhLgplgNjxFGR7BHL8cIJgJ5vhlfcR6TDBHbMACCJdgJpgDSCO6CFMBgjlMdc30TTATzGZmJqPitzJinAMEM8Ec4/SPRtc5Y47GOAUZJcFMMAeZT/QVggIEcwiiGu6SYCaYDU9Rhkcwxy8HCGaCOX5ZH7EeE8wRG7AAwiWYCeYA0oguwlSAYA5TXTN9E8wEs5mZyaj4rYwY5wDBTDDHOP2j0XXOmKMxTkFGSTAbDOZTp05h0&#43;bN2LJ5C/bt2xfkuNNXhBQgmCM0WAGFSjAbDObdu3dh4cISlC4uR2VlFT7&#43;&#43;OOAhp1uoqQAwRyl0QomVoLZUDB3dHTg7XfmY9myIqxY8SbmzStGY2NjMKNOL5FSgGCO1HAFEqw2mBOJBFSzWncrDyQ64UT6D&#43;Nh&#43;ZVJb&#43;KjqqoSf3trGYrnPYrVxd9CaWkx5i8oxdGjx0wMlzGFqIBJYA7rfAxRvki61gazCmJ7T8MaLAv6YShrMpj37t2Dt95aKOxNbFr1FTQ39cKSN4dhYXENampqwpCDPg1WwBQwh3k&#43;Gix/TkLLGMxhQdnrQhCEUqaCWX7gV1ZWhgULVmDRX2&#43;X7xmSVl/2D1haPFXMmt9FW1tbEBLQR0QUMAXMUq6wz/ewhyQq8fsGs31wwu5oWP5NBXNTU5NYT16KRYtmYuuaz/eAGacSKJ15PcorNglwr&#43;AHgWGfwQb5J5iDG4yweBJchF2eMgJzqk46vfWxl6V6e2S14VYv1bq3dSFxalfuM2mN&#43;dixowLKC1FevkEsXQztgvLRbhNgbljyJVSXT0RxyWo072wOOhfoz1AFVDC7nT&#43;pzkXdrumcb6ovp/NPfbfr57x1O1fdYndr260Pbp&#43;H&#43;fXj1j8vP/bxScXBtMCs85ZGDcQpKPvgphJfFcNpO9VM3h6PiTPmuro6lAjoLl82Dk1Vn0fBa1fh6ScHY9TTN&#43;D5X9&#43;I92vPRsWb38GqyvV4993lOHnypO75lrKe24ntpJtbkgUFh5TBxqyC24w51XnlVya3SY7buerVvu4&#43;P5zwYoYdxm5&#43;U2nm5cdJT11/Tsx0O1aWhwJmpxM06AGwi&#43;R0BfJKKNPAfODAAbF8UYq1tbVYtag/ThxJ4NFHR2LkyN/ggZH/hREjnkZF0QVorj4bKxf9CisrN2LDho1&#43;zz3P&#43;ronk5pkqRIz0ABj6swLzG6zwHSl8nPu2tv2urh7nf9OM1D7&#43;ex2vgdxMXHTUGei4eec8TN5jDyY1QH0ugI5vf2SZSYsZcgbR1atWoWNm3ZgdcUr2L6&#43;F453nIFfjrobP/3pkwLOD&#43;OeEf&#43;JVUUXAge&#43;gLI3rkDL7j0oXbICHR1H0j0HHY9TZww672p0kjfQAGPoTHfG7CWNLsD9glmnTbfZYiYXda&#43;88wNLP37c&#43;urnnNGdoEYazKkG1kswk2bMzc3NWPrualRWrcTmFecDp07DySNnimWMH&#43;Ouux/E8DvvxrBbb0NVkdjX&#43;jXsXnUWVsx5AHtaO1FVVRUoqtw0czq51JlOoEHQ2acUCALMupIGBeZU56ZT7vi5yPsBqi4MnWblOrpFBsx&#43;puxOb0W8ZmpuIju99YkCmOU6cVFRCfbsbUd50YPoaO4FdP4jju07C088diOG3jIcNw65BQMH/gBVC84Fdv4Tjm/&#43;Asqn90Zjw7tiSaM28N/R8JP0hLPOqZtZHTuY/YDAb8v28yjd2af9PPU6b91yyE8e6sLXrp2f/um&#43;O3DjV6q2VO19z5hVkDrB0G3JwOlq5DU7s&#43;CutmGHuNtrP2/Bc72UsXHjRqworxUz37nYuurrwKEvC/sGjrWdhUd&#43;eh36XTNY2Pdxff/rULPga8DWs3F0/ZfQXHIGls24GfsPfCi&#43;xbES8vvPQTxSjQlnzkGo7M&#43;H/VsZTueYn9lmKsCo516qttwmVl7nptdkLNVEzW88uoxwuiCl4psOZ9y0VC9GThcL32D2l1Lm1rbEyCWY5e9hLCoqFbPlg1j29m0CyGeLpYpvAvt649ieszFyxJW47PJrcPV3v4chN/ZHQ9FXgPd6oXPNmThak0DtG71QuegV1Kzdgm3btgUitg6YCedApNZ2ks3vMQcFeO3O5UnFoHUjmHP4WxnyRpENG5uxunwStlV8FWg7F6eaewPN56Jz81dw&#43;9Cv4QtfPAffuqAv&#43;n/vO6j725n4SAD5ULmw5cJKElj06sVirfmAWKNejuPHj2eU5qneajnNELyOySgYHtyjAMFsdjIEDWXZW4I5R2CWv6&#43;8qGgptmzZjPK/DQJ29cKxxrNwZP3pYqkigYNVCdx4bdcPR/X6/Jdx3TWXYe3M09C5LIH9xcIWCDALqx3XS3wQ&#43;Axq1m3F5k2b085gt&#43;Syr905vb3z8/Yz7QBjfGC2wMxx9JdkfpY6/HkmmHPydTn5gV9x8WJseW8XFs8ZiX1Vp&#43;PIWgHa6gQOrEqgozKBdvE8rH8XmL/&#43;jT64/bb&#43;WD/7NBxZLKA8T9jcBPa9I57fSqD49&#43;egZmUpFi8pE1&#43;f6/CbA6xvuALZArPhMsQqPM6YczBj/uCDD7BgofhJz&#43;I3Uf/W2fhQQFnOkA8KGB&#43;oEFBemcCH4vU9QyWYP4cL&#43;34Xd90xGBv&#43;IsAsZ8sCyPveTqDtTfEs7L3xZ2Dh67diVU0jVq9eHasEjkNnCeY4jPKn&#43;0gwZxnMx44dw7z5C1Gxsh4lf/4RWpefjoNihnxAwFhC&#43;cAKYXL9uEz8oty0BOa/KH686NUEqgsT2DWna6bcJmbJrQLIrbMT2DNLlP0lgRW/74WSOX9AUWk59u7dG79MzuMeE8x5PLguXSOYswzm1asrUbKkBgvf&#43;RM2vtULhySUxYd5FpD3LxWgLRWzZmFrJyfwpycSeGFkLyx6OYEjYk25TcBZArlVwLhlprAZwt5I4P2CMzD3lX/BipXrxb&#43;elMUvk/O4xwRzHg8uwfxpBXLxdbnDhw&#43;Lv4taJG4oKUbpnwehbdnpXbNkMUM&#43;ID7UOyChLNaQDwo7IrZ/eZdcyjgT5/S&#43;Cg/cdQ2apos6cqYsoCxnykkod1ubgPRKMWueO3kUFpWuhFwu4SM/FCCY82Mc/fSCM&#43;YszpiXLFmKBUUVmC&#43;&#43;RbHlnV7iQ74zcWilmDWXi2e5pFF2Gg4t6YJyp1jKuOMGCeazcPW1g3HvPXeifqI4Zr5cyvicWF8&#43;E61zeiWtbc6ZYq25F/4&#43;/Qyx9HExlpQuw0Lx/Wi5bMJH9BUgmKM/hn57QDBnCcx79rSIv4qaj7lvT0X5nBvx8XsX49j6y3Bs3aU4VvttHFt7CY6u6YtjNaK8ug9QeyGqpn8L/zqoL&#43;74yVBM&#43;r/rsa/4IhxdcSk6ll6G9qWXo2OZtMuSdkTY8RXfRv2Ub&#43;Cd8Q9g3oJlqK9v8JsPrG&#43;gAgSzgYMSckgEc5bAvHhxKYqKy8TfRT2FNTMuRM0bl6BmmrApwib3VexiVE/uk7RaYRUFfVA6&#43;gKsLBCgLrwYlYWXoHJCt4ntKsWqxfYaccyS1/th8YJ3MHd&#43;EQ4ePBRyCtF92AoQzGErbJ5/gjlLYJ43fwGqqtegoa4K6&#43;uqhdVgQ91abKyvxaaGOmxt3ISd27aitWU3DrS14uB&#43;aW04JO1AW3Jb2v7WFrTs2omW5p3Y1rgFWzbUJ4&#43;XfqS/9bVdftfV1mHp0rLAf33OvBTO/4gI5vwfY3sPCeYsgbm0tBRTpkzFuHHjhRV2WUEhCsZPxPjCiSicOBkTJ07DxEnTMGnadEyb9kaXTZ&#43;B6cLks1U2WeyfNFnUmzRV1J&#43;MCYWTMH7CpKSvpM&#43;kjcesWbOwffv2&#43;GV1nvWYYM6zAdXoDsGcJTDLsaivr8&#43;6aeQAqxiuAMFs&#43;ACFEB7BnEUwhzB&#43;dBkDBQjmGAyyrYsEM8Ecv6yPWI8J5ogNWADhEswEcwBpRBdhKkAwh6mumb4JZoLZzMxkVD0KEMzxSwaCmWCOX9ZHrMcEc8QGLIBwCWaCOYA0ooswFSCYw1TXTN8EM8FsZmYyKi5lxDgHCGaCOcbpH42uc8YcjXEKMkqCmWAOMp/oKwQFCOYQRDXcJcFMMBueogyPYI5fDhDMBHP8sj5iPSaYIzZgAYSrBWb1b83Vv&#43;x2Kw8grtBd5OIfTELvFBvISwUI5rwcVs9OaYHZ8mDBzO7RrdxkOQlmk0eHsakKEMzxyweCmUsZ8cv6iPWYYI7YgAUQLsFMMAeQRnQRpgIEc5jqmuk7NDCr689q173KZT37Gra9LF1fbr5l0vNBBUxWgGA2eXTCiS0UMKtrzm7bFihVYNrrqpBW69u3dX1ZEqp&#43;CeZwEoteg1OAYA5Oy6h4Ch3MbjNcL&#43;iqALUf7yRsKl/2DycJ5qikJ&#43;OUChDM8cuDUMDstvzg9e0Np31OQHUCve5smjPm&#43;CV4PvSYYM6HUfTXh9DA7DTrDRLMXtD2A3QuZfhLGNbOvgIEc/Y1z3WLvsDstbbrtuSgs8bs5DdVW07r0bpLIFzKyHXasX0/ChDMftTKj7q&#43;wawuU3jdcGJf97UvI&#43;h80Od2l6FTDDp3JzodxxlzfiRyPveCYM7n0XXuW1pgzgeZeOdfPoxiPPpAMMdjnNVeEsz8HnP8sj5iPSaYIzZgAYRLMBPMAaQRXYSpAMEcprpm&#43;iaYCWYzM5NR9ShAMMcvGQhmgjl&#43;WR&#43;xHhPMERuwAMIlmAWYDx06RKMGxuYAwRwA6SLmgmAmmI0FEi&#43;YXRMGgjliVA0gXIKZYCaYDX&#43;3QDAHQLqIuSCYCWaCmWCOGLbyP1yCmWAmmAnm/CddxHpIMBPMBDPBHDFs5X&#43;4WmB2&#43;h2KKP4Bqzqc6i3Z/JCJ30oxOQe4xpz/ILb3UAvM1kGpfk4zSvIRzISxyTBWYyOYo0SWYGLNCMwyhKjOnAlmgplgDgYi9BK8AgQz15i5xsw15uDJQo8ZKRA4mNX1aHtkbvvs5X5fq0stXn9RZf99Z3mcfJsYlZkT44znLJ9LGRkxLpIHZwTmVGvObv9e4rfcaclE96&#43;lvC4GBHM8QRe1CxzBHEm2ZhS0bzB7zYjt33rw&#43;lsptw8UdT9oTOVbdz9nzISz6aAmmDNiXCQP9g3mVL10Wi7QWWpItQShzppTzdTtM2wv3wSzPpinvrsBw0cvxvefn0vLQAOpodRS94JAMKeiTv7tDxTMOsD0&#43;iaH2/FWuf3Za9bNGbM&#43;cHUAMXnJegwYuwJDag7jpi0f0TLQQGootZSa6mhPMOcfeFP1KDQwqzNnrxmsF0CdfLh9PS&#43;VH6cPIrnGrA/v2/9YgiFrDuPhrcdx&#43;umno729nc9p6jDhyi8mtZSaEsypEBXP/VpgVteV7bB0WldWZ7b2badZcSqf9jbchsrJDz/804evFyTk8sVNmz/qgbEEMy19DaSWUlOCOZ7gTdVrLTCnchLF/dYFgmvMeuC2wHz48GHQMtPgtNNOS17kCOYokiM7MRPM/B6z1qzNArO8oOnM8ljH&#43;4JHMGcHcFFthWAmmLVAK8H8w00ncPDgQVqGGlxwwQVJLTljjio2w4&#43;bYCaYfYFZQsUkOKufIdjjkvt0ynLRH4I5fLhFuQWCmWD2Beb9&#43;/fDFJPgVWPx&#43;zpX/Tj//PM5Y44yNbMQO8FMMGuDecim4zjvvPOwb9&#43;&#43;nJuEsFMcarnbtgnxSy25lJEFwkW0CYKZYNYH88bjaGtrM8IkdJ1iUcutbbe6uepL7969MURoSTBHlJpZCNsXmMeMGYNnnnkGTzzxROSNX5fT&#43;5qc9e0KCZEb6jtx5ZVXYuvWrTk3OX5Ocajl1vqzCfHaY5BaEsxZIFxEm9AG80svv4xXX30Ve/bswcmTJyNvBHN6YDYFcrpglvG61c1VX&#43;TFjWCOKDGzFLY2mEeNGoXW1lacOHEiL&#43;74Ipj9g/kH647giiuuQFNTU85Njp9THGq527YJ8UstOWPOEuUi2Iw2mOXyhZwp58ttuARzemBubGyEKSbHUI3F7&#43;tc9ePyyy8HwRxBWmYxZF9g/uijj/LmdlyC2T&#43;YB67rwKWXXootW7YYY&#43;r3mO1xyX06Zbnoj9SSM&#43;Yski5iTfkGc77cakswpwHmug5s2rSJlqEGl1xyCQYKLQnmiNEyi&#43;H6BrPuXVLqTMba9nOsbl2vek53fln1Ceb0wCyhQjhnfnEimLNIuQg25QvM8oM/nbul7HdgWce4ldt96tbTicWtbYLZP5gH1LZjw4YNtAw16Nu3L6SWnDFHkJhZCtk3mHXumlLvuFLru5XbferW04nFqmP3STCnB&#43;Y&#43;ffqgoaGBlqEGBHOWCBfRZnyDWeduKZ07rex11NfWtrUEorbptk&#43;nrr0NOWb8PWY9QMvZ3fVrD6O&#43;vp6WoQYXXXRRUkvOmCNKzSyE7QvMH374ofYdX&#43;oac6o7tOw3AVjHWsfJ1&#43;p2qtdux6lxcMasB2Trw175N0jXl&#43;/FnP7fRF1dHQYMGMDnNHW4e21rUkv&#43;tVQWCBfRJkIDsx2CKkyd7sayw9Z&#43;vBNs/fhxap8zZn04yz8O/f6rZehfsRf91x6iZaKB0FBqyT9jjSg1sxC2bzBncteUhKN1vLoty4LY5&#43;XHqT2CWR/McuZcWNqAH79SnHwLTktfA6mh1FL3q6f8l&#43;wskNCwJnyDWeduKQlBp3pqub1OEPtkm25&#43;nNojmP2BWRckrBesrgSzYdTMQji&#43;wNzZ2al1x5eEoNPdVGq5vU4Q&#43;2Sbbn6c2iOYgwUIgRyOngRzFkhoWBO&#43;wax7c4H64Z&#43;1bT/WXsfa71Yu98t9qh&#43;v1&#43;o&#43;ewz88C8ciBDOwetKMBtGzSyE4wvMR44cyZubCwjm4AFCKIejKcGcBRIa1oRvMOfLzQUEczgQIZyD15VgNoyaWQjHF5g7Ojry5uYCgjl4gBDK4WhKMGeBhIY1oQ1m&#43;UP5O3bsSP4BprzBIOpGMIcDEcI5eF0JZsOomYVwtMH8svhrqdGjR6O5uRly5hx1I5iDBwihHI6mBHMWSGhYE9pglnG/&#43;OKLkDNn/hlrOCcgwUZdnXKAYDaMmlkIxxeYsxBP1prgjJkQjMqFkGDOGhaMaYhgrm/VvjU2Kicy48yviw7BbAwvsxYIwUww88IkfgfE5IsZwZw1HhrTEMEswCwTn0YNTM6BS&#43;uak3e98hEPBWINZpnsNGoQlRwgmOMBZdnLWIPZ6fc8WJZIzsxoZmoQHzTFu6exBXO8h529pwJUwGQFCGaTR4exUQEqEEsFCOZYDjs7TQWogMkKEMwmjw5jowJUIJYKEMyxHHZ2mgpQAZMVIJhNHh3GRgWoQCwVIJhjOezsNBWgAiYrMHjwYPz85z/HhAkTUFNTA95aZPJoMTYqQAViocDAgQPx0EMPoaCgANXV1QRzLEadnaQCVMBoBa677jrcf//9GDt2LCorKwlmo0eLwVEBKhALBfr164f77rsPY8aMIZhjMeLsJBWgAsYr8JmlDPmXUU1NTVi8eDFeeukljBgxAoMGDYKcWvfv3x9yUfqmm27CzTffTKMGzAHmAHMggBy45ZZbMGzYMNx6662Q28OHD8ezzz6LGTNmJP8AO9HZ2YmdO3cmp8/Tpk3DCy&#43;8gIcffji53vHggw8mPyl8/PHHadSAOcAcYA4ElANPPvkkfvGLX&#43;Cpp56C3H7uuecwbtw4FBUVobGxEYnjx48n/wlbvigrK8Ps2bNRWFiI119/PfkJ4cSJEzF58mQaNWAOMAeYAwHlwJQpUzB16tTkZFhuz5o1CyUlJairq0NLSwsSJ0&#43;ehJw1Szh/8MEHaGhoSH6PTs6gq6qqkttr1qzB2rVradSAOcAcYA4ElAO1tbWQJtkqly/kkvLu3bvR3t6O/wcxHpJcpjMipAAAAABJRU5ErkJggg&#61;&#61;\" style=\"width:358px;height:190px;margin-top:0px;margin-bottom:0px;margin-left:0px;margin-right:0px;border:0px solid black;\" /></p>\n\n<ol><li>Navigate to www.fusesoft.co</li><li>Update this param</li></ol>\n\n<pre>&#34;&gt;&lt;script&gt;alert(123);</pre>\n";
	
	
	
	@Test
	public void test() throws IOException {
		File file = new File("C:/tmp/data.out");
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		String report = new String(data);
		
		
		
		Calendar oldDate = Calendar.getInstance();
		oldDate.set(Calendar.YEAR, 2012);
		oldDate.set(Calendar.MONTH, 1);
		oldDate.set(Calendar.DATE, 1);
		//List<User> users = new ArrayList<User>();
		HibHelper.getInstance().getEMF().createEntityManager();
		for(int i=0; i< 20000; i++){
			String uuid = UUID.randomUUID().toString();
			System.out.println(uuid);
			
			Date now = oldDate.getTime();
			oldDate.add(Calendar.DATE, 1);
			System.out.println(oldDate);
			System.out.println("Creating Assessment " + i);
			EntityManager em = HibHelper.getInstance().getEM();
			HibHelper.getInstance().preJoin();
			em.joinTransaction();
			Teams t = em.find(Teams.class, 12l);
			
			
			User u = em.find(User.class, 2l);
			Assessment a = new Assessment();
			a.setAssessor(new ArrayList<User>());
			a.getAssessor().add(u);
			a.setAppId(uuid);
			a.setName(uuid);
			a.setDistributionList(uuid);
			a.setStart(oldDate.getTime());
			a.setEnd(oldDate.getTime());
			a.setEngagement(u);
			a.setEngagement(u);
			a.setSummary(uuid);
			a.setRiskAnalysis(uuid);
			em.persist(a);
			a.setVulns(new ArrayList<Vulnerability>());
			for(int k=0; k<10; k++){
				Vulnerability v = new Vulnerability();
				v.setName(uuid);
				v.setDescription(uuid);
				v.setRecommendation(uuid);
				v.setAssessmentId(a.getId());
				v.setAssessorId(u.getId());
				v.setOpened(oldDate.getTime());
				v.setOverall(3l);
				v.setLikelyhood(3l);
				v.setImpact(3l);

				
				
				ExploitStep ex = new ExploitStep();
				ex.setDescription(editorData);
				ex.setUpdated(oldDate.getTime());
				ex.setCreator(u);
				em.persist(ex);
				em.persist(v);
				a.getVulns().add(v);
				em.persist(a);
			}
	
			FinalReport fr = new FinalReport();
			fr.setBase64EncodedPdf(report);
			fr.setGentime(oldDate.getTime());
			fr.setFilename(uuid);
			a.setFinalReport(fr);
			a.setCompleted(oldDate.getTime());
	
			HibHelper.getInstance().commit();
			em.close();
		}
		HibHelper.getInstance().getEMF().close();
	}

}
