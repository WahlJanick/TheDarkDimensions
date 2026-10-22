"""Keep source geometry intact and export only explicit CEM body-variant rules."""
def geometry(node):
    return {k:([geometry(x) for x in v] if k=='submodels' else geometry(v) if k=='submodel' else v) for k,v in node.items() if k!='animations'}

def visibility_rules(nodes):
    result={}
    def visit(node):
        for animation in node.get('animations',[]):
            for target,expression in animation.items():
                if not target.endswith('.visible') or target.startswith('health_bar.'):continue
                # These are the actual state variables used by this pack. Do not
                # infer a switch from an ID suffix: scorpion legs are always visible.
                if 'var.glowing_eyes' in expression:rule='calm' if '== 0' in expression else 'aggressive'
                elif 'varb.wet' in expression:rule='dry' if expression.startswith('!') else 'wet'
                elif 'var.vexed' in expression:rule='calm' if '== 0' in expression else 'aggressive'
                else:raise ValueError(f'Unsupported original CEM visibility rule: {target} = {expression}')
                result[target.removesuffix('.visible')]=rule
        for child in node.get('submodels',[]):visit(child)
        if 'submodel' in node:visit(node['submodel'])
    for node in nodes:visit(node)
    return result
